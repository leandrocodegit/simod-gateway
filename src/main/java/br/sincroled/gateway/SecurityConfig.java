package br.sincroled.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
 import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/modulo/**").access(clientIdMatchesTenantClaim())
                        .pathMatchers("/agenda/**").access(clientIdMatchesTenantClaim())
                        .pathMatchers("/usuario/**").access(clientIdMatchesTenantClaim())
                        .anyExchange().permitAll()).build();
    }

    @Bean
    public WebFilter jwtDecoderWebFilter() {
        return new DynamicJwtDecoderWebFilter();
    }
    private ReactiveAuthorizationManager<AuthorizationContext> clientIdMatchesTenantClaim() {
        return (authenticationMono, context) -> authenticationMono.map(auth -> {
            if (auth.getPrincipal() instanceof Jwt jwt) {
                List<String> tenants = jwt.getClaimAsStringList("tenants");

                List<String> roles = jwt.getClaimAsStringList("roles");
                if(!roles.contains("client") && !roles.contains("admin"))
                    return new AuthorizationDecision(false);

                String clientId = context.getExchange().getRequest().getHeaders().getFirst("X-Tenant-ID");
                String issuerUri = "http://localhost:8080/realms/" + clientId;
                ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
                boolean authorized = clientId != null && tenants != null && tenants.contains(clientId);
                return new AuthorizationDecision(authorized);
            }
            return new AuthorizationDecision(false);
        });
    }

    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
            converter.setAuthoritiesClaimName("roles");
            converter.setAuthorityPrefix("ROLE_");

            Map<String, Object> resourceAccess = jwt.getClaimAsMap("realm_access");
            List<String> roles = (List<String>) resourceAccess.get("roles");



            Collection<GrantedAuthority> authorities =  roles.stream().map(role -> new GrantRole(role)).collect(Collectors.toList());

            // Log para debug
            System.out.println("=== Roles extraídas do token ===");
            authorities.forEach(auth -> System.out.println(auth.getAuthority()));

            return Mono.just(new JwtAuthenticationToken(jwt, authorities));
        };
    }

    private class GrantRole implements GrantedAuthority{

        private String role;

        public GrantRole(String role) {
            this.role = role;
        }

        @Override
        public String getAuthority() {
            return "ROLE_" + role;
        }
    }
}