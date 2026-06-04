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
                            .pathMatchers("/actuator/**").hasRole(ROLE_ADMIN)
                            .pathMatchers("/api/reports", "/api/reports/**")
                            .hasAnyRole(ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers("/api/sla", "/api/sla/**")
                            .hasAnyRole(ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers("/api/workflows", "/api/workflows/**")
                            .hasAnyRole(ROLE_AGENT, ROLE_ADMIN)
                            .pathMatchers("/api/agent/tickets", "/api/agent/tickets/**")
                            .hasAnyRole(ROLE_AGENT, ROLE_ADMIN)
                            .pathMatchers("/api/tickets", "/api/tickets/**")
                            .hasAnyRole(ROLE_CUSTOMER, ROLE_ADMIN)
                            .pathMatchers("/api/products", "/api/products/**")
                            .hasAnyRole(ROLE_CUSTOMER, ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers("/api/ticket-topics", "/api/ticket-topics/**")
                            .hasAnyRole(ROLE_CUSTOMER, ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers("/api/organization", "/api/organization/**")
                            .hasAnyRole(ROLE_AGENT, ROLE_MANAGER, ROLE_ADMIN)
                            .pathMatchers("/api/files", "/api/files/**", "/api/notifications", "/api/notifications/**")
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
