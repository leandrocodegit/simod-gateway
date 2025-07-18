package br.sincroled.gateway;

import br.sincroled.gateway.models.ControleAcesso;
import br.sincroled.gateway.models.DiasSemana;
import com.nimbusds.jose.shaded.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DynamicJwtDecoderWebFilter implements WebFilter {

    @Value("${keycloak-issuer}")
    private String issuer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String realm = exchange.getRequest().getHeaders().getFirst("X-Tenant-ID");
        var host = exchange.getRequest().getPath().pathWithinApplication().value();

        if (host.equals("/cliente") || host.contains("/validate"))
            return chain.filter(exchange);

        if (false && realm == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if(authHeader == null && exchange.getRequest().getQueryParams().containsKey("token")){
           authHeader = "Bearer " + exchange.getRequest().getQueryParams().get("token").get(0);
        }

        if (authHeader != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            addCors(exchange);
            String token = authHeader.substring(7);
            JwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer + realm);

            try {
                Jwt jwt = jwtDecoder.decode(token);
                String sub = jwt.getClaimAsString("sub");

                List<String> origins = jwt.getClaimAsStringList("allowed-origins");
                String hostDestino = origins.stream().filter(origin -> origin.contains("camunda")).findFirst().get();
                Map<String, Object> controleAcesso = jwt.getClaim("controle_acesso");
                Boolean controleAcessoAtivo = (Boolean) controleAcesso.get("ativo");

                if (controleAcessoAtivo != null && controleAcessoAtivo.equals(Boolean.TRUE)) {
                    Boolean controlarDias = (Boolean) controleAcesso.get("controlarDias");

                    if (controlarDias != null && controlarDias.equals(Boolean.TRUE)) {
                        var diaHoje = LocalDateTime.now().getDayOfWeek().getValue();
                        List<String> diasStr = (List<String>) controleAcesso.get("dias");
                        List<DiasSemana> controleAcessoDias = diasStr.stream()
                                .map(DiasSemana::valueOf)
                                .collect(Collectors.toList());
                        if (controleAcessoDias.stream().noneMatch(dia -> dia.dia == diaHoje)) {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }

                        Boolean controlarHorario = (Boolean) controleAcesso.get("controlarHorario");

                        if (controlarHorario != null && controlarHorario.equals(Boolean.TRUE)) {
                            LocalTime horaAtual = LocalTime.now();
                            Long tolerancia = (Long) controleAcesso.get("tolerancia");
                            if (tolerancia == null)
                                tolerancia = 0L;
                            LocalTime controleAcessoInicio = LocalTime.parse((String) controleAcesso.get("inicio"));
                            LocalTime controleAcessoFim = LocalTime.parse((String) controleAcesso.get("fim"));
                            if (!controleAcessoInicio.equals(controleAcessoFim)) {
                                var anterior = horaAtual.plusMinutes(tolerancia).isBefore(controleAcessoInicio);
                                var posterior = horaAtual.minusMinutes(tolerancia).isAfter(controleAcessoFim);
                                if (anterior || posterior) {
                                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                                    return exchange.getResponse().setComplete();
                                }
                            }
                        }
                    } else {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                if (realm == null) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (!exchange.getRequest().getHeaders().containsKey("X-User-ID"))
                    exchange.getRequest().getHeaders().add("X-User-ID", sub);
                if (!exchange.getRequest().getHeaders().containsKey("X-Engine-Origin"))
                    exchange.getRequest().getHeaders().add("X-Engine-Origin", hostDestino.replace("camunda-", ""));
                AbstractAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                SecurityContext context = new SecurityContextImpl(authentication);
                return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
            } catch (JwtException ex) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange);
    }

    private void addCors(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getResponse().getHeaders();
        if (!headers.containsKey("Access-Control-Allow-Origin")) {
            headers.add("Access-Control-Allow-Origin", "*"); // ou origem dinâmica
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "*");
            headers.add("Access-Control-Allow-Credentials", "false");
        }
    }

    private ReactiveAuthorizationManager<AuthorizationContext> clientIdMatchesTenantClaim() {
        return (authenticationMono, context) -> authenticationMono.map(auth -> {
            if (auth.getPrincipal() instanceof Jwt jwt) {

                List<String> roles = jwt.getClaimAsStringList("roles");
                if (!roles.contains("client") && !roles.contains("admin"))
                    return new AuthorizationDecision(false);

                String clientId = context.getExchange().getRequest().getHeaders().getFirst("X-Tenant-ID");
                String issuerUri = "http://localhost:8080/realms/" + clientId;
                ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
                boolean authorized = clientId != null;
                return new AuthorizationDecision(authorized);
            }
            return new AuthorizationDecision(false);
        });
    }
}
