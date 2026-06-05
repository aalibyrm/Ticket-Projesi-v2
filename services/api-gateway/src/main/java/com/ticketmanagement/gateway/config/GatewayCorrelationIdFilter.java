package com.ticketmanagement.gateway.config;

import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
class GatewayCorrelationIdFilter implements WebFilter {

    static final String HEADER_NAME = "X-Correlation-Id";
    private static final String CONTEXT_KEY = "correlationId";
    private static final Pattern SAFE_CORRELATION_ID = Pattern.compile("[A-Za-z0-9._:-]{1,160}");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = resolveCorrelationId(exchange.getRequest());
        ThreadContext.put(CONTEXT_KEY, correlationId);
        exchange.getResponse().getHeaders().set(HEADER_NAME, correlationId);
        ServerWebExchange correlatedExchange = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .headers(headers -> headers.set(HEADER_NAME, correlationId))
                        .build())
                .build();
        return chain.filter(correlatedExchange)
                .doFinally(signalType -> ThreadContext.remove(CONTEXT_KEY));
    }

    private static String resolveCorrelationId(ServerHttpRequest request) {
        String headerValue = request.getHeaders().getFirst(HEADER_NAME);
        if (headerValue == null || headerValue.isBlank()) {
            return UUID.randomUUID().toString();
        }
        String candidate = headerValue.trim();
        if (!SAFE_CORRELATION_ID.matcher(candidate).matches()) {
            return UUID.randomUUID().toString();
        }
        return candidate;
    }
}
