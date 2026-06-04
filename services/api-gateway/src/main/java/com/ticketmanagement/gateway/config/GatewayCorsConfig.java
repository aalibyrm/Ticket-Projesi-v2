package com.ticketmanagement.gateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
class GatewayCorsConfig {

    @Bean
    CorsWebFilter corsWebFilter(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}") String allowedOrigins) {
        List<String> origins = splitCsv(allowedOrigins);
        validateAllowlist(origins);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "authorization",
                "Content-Type",
                "content-type",
                "X-Correlation-Id",
                "x-correlation-id"));
        config.setExposedHeaders(List.of("X-Correlation-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    private static List<String> splitCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }

    private static void validateAllowlist(List<String> origins) {
        if (origins.isEmpty()) {
            throw new IllegalArgumentException("At least one CORS origin must be configured");
        }
        if (origins.contains("*")) {
            throw new IllegalArgumentException("Wildcard CORS origins are not allowed with credentials");
        }
    }
}
