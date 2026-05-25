package com.ticketmanagement.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
class GatewaySecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            @Value("${app.security.jwt.enabled:true}") boolean jwtEnabled,
            Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
        if (jwtEnabled) {
            return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .cors(Customizer.withDefaults())
                    .authorizeExchange(exchanges -> exchanges
                            .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                            .pathMatchers("/actuator/**").hasRole("ADMIN")
                            .anyExchange().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
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
