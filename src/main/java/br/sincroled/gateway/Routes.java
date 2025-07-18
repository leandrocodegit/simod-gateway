package br.sincroled.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class Routes {

    private final RoutesConfig routesConfig;
    private final RouteFilter filter;

    public Routes(RoutesConfig routesConfig, RouteFilter filter) {
        this.routesConfig = routesConfig;
        this.filter = filter;
    }

    @Bean
    public RouteLocator routesCloudaflare(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("validate", r ->
                        r.path("/validate/**").uri("http://localhost:9082/")
                ).build();
    }


    @Bean
    public RouteLocator testeApiA(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("modelos", r ->
                        r.path("/modelos/**").filters(filter ->
                            filter.rewritePath("/modelos","/modelos")
                        ).uri(routesConfig.getMap().get("modelos"))
                ).build();
    }

    @Bean
    public RouteLocator agendaApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("agenda", r ->
                        r.path("/agenda/**").uri("http://localhost:9081/")
                ).build();
    }

    @Bean
    public RouteLocator usuarioApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("usuario", r ->
                        r.path("/usuario/**").uri(routesConfig.getMap().get("usuario"))
                ).build();
    }

    @Bean
    public RouteLocator meuUsuarioApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("conta", r ->
                        r.path("/conta/**").uri(routesConfig.getMap().get("conta"))
                ).build();
    }

    @Bean
    public RouteLocator formularioApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("formulario", r ->
                        r.path("/formulario/**").uri(routesConfig.getMap().get("formulario"))
                ).build();
    }

    @Bean
    public RouteLocator deployFormularioApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("deploy", r ->
                        r.path("/deploy/**").uri(routesConfig.getMap().get("formulario"))
                ).build();
    }

    @Bean
    public RouteLocator clienteApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("cliente", r ->
                        r.path("/cliente/**").uri("http://localhost:9084/")
                ).build();
    }

    @Bean
    public RouteLocator protocoloApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("protocolos", r ->
                        r.path("/protocolos/**").filters(filter ->
                                filter.rewritePath("/protocolos","/"))
                                        .uri(routesConfig.getMap().get("protocolo"))
                ).build();
    }

    @Bean
    public RouteLocator bpmnApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("processo", r ->
                        r.path("/processo/**").filters(filter ->
                                filter.rewritePath("/processo","/")
                        ).uri("http://localhost:9002/")
                ).build();
    }

    @Bean
    public RouteLocator fileApi(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("file", r ->
                        r.path("/file/**").filters(filter ->
                                filter.rewritePath("/file","/engine-rest")
                        ).uri("http://localhost:8085/")
                ).build();
    }

}
