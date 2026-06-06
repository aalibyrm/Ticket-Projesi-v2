package com.ticketmanagement.gateway.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
class GatewaySecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_AGENT = "AGENT";
    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String[] OPENAPI_ROUTES = {
            "/v3/api-docs", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"
    };
    private static final String[] REPORT_ROUTES = {
            "/api/reports", "/api/reports/**", "/api/v1/reports", "/api/v1/reports/**"
    };
    private static final String[] SLA_ROUTES = {
            "/api/sla", "/api/sla/**", "/api/v1/sla", "/api/v1/sla/**"
    };
    private static final String[] WORKFLOW_ROUTES = {
            "/api/workflows", "/api/workflows/**", "/api/v1/workflows", "/api/v1/workflows/**"
    };
    private static final String[] AGENT_TICKET_ROUTES = {
            "/api/agent/tickets", "/api/agent/tickets/**", "/api/v1/agent/tickets", "/api/v1/agent/tickets/**"
    };
    private static final String[] CUSTOMER_TICKET_ROUTES = {
            "/api/tickets", "/api/tickets/**", "/api/v1/tickets", "/api/v1/tickets/**"
    };
    private static final String[] PRODUCT_ROUTES = {
            "/api/products", "/api/products/**", "/api/v1/products", "/api/v1/products/**"
    };
    private static final String[] TICKET_TOPIC_ROUTES = {
            "/api/ticket-topics", "/api/ticket-topics/**", "/api/v1/ticket-topics", "/api/v1/ticket-topics/**"
    };
    private static final String[] ORGANIZATION_ROUTES = {
            "/api/organization", "/api/organization/**", "/api/v1/organization", "/api/v1/organization/**"
    };
    private static final String[] AUTHENTICATED_ROUTES = {
            "/api/files", "/api/files/**", "/api/notifications", "/api/notifications/**",
            "/api/v1/files", "/api/v1/files/**", "/api/v1/notifications", "/api/v1/notifications/**"
    };

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            @Value("${app.security.jwt.enabled:true}") boolean jwtEnabled,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter,
            ObjectProvider<ReactiveJwtDecoder> jwtDecoder) {
        if (jwtEnabled) {
            ReactiveJwtDecoder configuredJwtDecoder = jwtDecoder.getObject();
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .cors(Customizer.withDefaults())
                    .authorizeExchange(exchanges -> exchanges
                            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                            .pathMatchers(OPENAPI_ROUTES).permitAll()
                            .pathMatchers("/actuator/**").hasRole(ROLE_ADMIN)
                            .pathMatchers(REPORT_ROUTES)
                            .hasAnyRole(ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers(SLA_ROUTES)
                            .hasAnyRole(ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers(WORKFLOW_ROUTES)
                            .hasAnyRole(ROLE_AGENT, ROLE_ADMIN)
                            .pathMatchers(AGENT_TICKET_ROUTES)
                            .hasAnyRole(ROLE_AGENT, ROLE_ADMIN)
                            .pathMatchers(CUSTOMER_TICKET_ROUTES)
                            .hasAnyRole(ROLE_CUSTOMER, ROLE_ADMIN)
                            .pathMatchers(PRODUCT_ROUTES)
                            .hasAnyRole(ROLE_CUSTOMER, ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers(TICKET_TOPIC_ROUTES)
                            .hasAnyRole(ROLE_CUSTOMER, ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers(ORGANIZATION_ROUTES)
                            .hasAnyRole(ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers(AUTHENTICATED_ROUTES)
                            .authenticated()
                            .anyExchange().denyAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                            .jwtDecoder(configuredJwtDecoder)
                            .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                    .build();
        }

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

    @Bean
    Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(new GatewayJwtRealmRoleConverter()));
        return converter;
    }
}
