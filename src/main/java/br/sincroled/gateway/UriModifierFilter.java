package br.sincroled.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;


public class UriModifierFilter implements GlobalFilter, Ordered {
    @Value("${keycloak-issuer}")
    private String issuer;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String[] hostDestino = new String[4];
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        String realm = exchange.getRequest().getHeaders().getFirst("X-Tenant-ID");
        var host = exchange.getRequest().getPath().pathWithinApplication().value();

        if (!host.contains("/engine-rest"))
            return chain.filter(exchange);

        if (authHeader != null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            JwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer + realm);

            try {
                Jwt jwt = jwtDecoder.decode(token);
                String sub = jwt.getClaimAsString("sub");
                Map<String, Object> controleAcesso = jwt.getClaim("controle_acesso");
                Boolean controleAcessoAtivo = (Boolean) controleAcesso.get("ativo");
                List<String> origins = jwt.getClaimAsStringList("allowed-origins");

                hostDestino = origins.stream().filter(origin -> origin.contains("camunda")).findFirst().get().split(":");
                System.out.println(hostDestino);
            } catch (JwtException ex) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        var url = hostDestino[1].replace("//camunda-", "");
        var porta = Integer.parseInt(hostDestino[2]);
        URI novaUri = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(url)
                .port(porta)
                .path(exchange.getRequest().getURI().getRawPath())
                .query(exchange.getRequest().getURI().getRawQuery())
                .build(true)
                .toUri();

        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, novaUri);
        return chain.filter(exchange);
    }

    private String resolveHost(ServerWebExchange exchange) {
        // Lógica de resolução dinâmica — pode usar headers, token, path, etc
        return "localhost"; // Exemplo simples
    }

    @Override
    public int getOrder() {
        return -1; // Executar cedo
    }
}
