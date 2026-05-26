package com.ticketmanagement.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
class GatewaySecurityHeadersConfig {

    @Bean
    WebFilter gatewaySecurityHeadersWebFilter() {
        return new SecurityHeadersWebFilter();
    }

    private static final class SecurityHeadersWebFilter implements WebFilter, Ordered {

        private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
        private static final String FRAME_OPTIONS = "X-Frame-Options";
        private static final String PERMISSIONS_POLICY = "Permissions-Policy";
        private static final String REFERRER_POLICY = "Referrer-Policy";
        private static final String TRANSPORT_SECURITY = "Strict-Transport-Security";
        private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 30;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set(CONTENT_SECURITY_POLICY, "default-src 'none'; frame-ancestors 'none'; base-uri 'none'");
            headers.set(FRAME_OPTIONS, "DENY");
            headers.set(PERMISSIONS_POLICY, "camera=(), microphone=(), geolocation=(), payment=()");
            headers.set(REFERRER_POLICY, "no-referrer");
            headers.set(TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");
            headers.set(X_CONTENT_TYPE_OPTIONS, "nosniff");
            return chain.filter(exchange);
        }
    }
}
