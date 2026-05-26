package com.ticketmanagement.gateway.config;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
class GatewayRateLimitConfig {

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    WebFilter fixedWindowRateLimitWebFilter(
            @Value("${app.rate-limit.capacity:120}") int capacity,
            @Value("${app.rate-limit.window:PT1M}") Duration window) {
        return new FixedWindowRateLimitWebFilter(capacity, window, Clock.systemUTC());
    }

    private static final class FixedWindowRateLimitWebFilter implements WebFilter, Ordered {

        private static final String UNKNOWN_CLIENT = "unknown";
        private static final String RATE_LIMIT_LIMIT = "X-RateLimit-Limit";
        private static final String RATE_LIMIT_REMAINING = "X-RateLimit-Remaining";
        private static final String RATE_LIMIT_RESET = "X-RateLimit-Reset";

        private final int capacity;
        private final long windowMillis;
        private final Clock clock;
        private final ConcurrentMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

        private FixedWindowRateLimitWebFilter(int capacity, Duration window, Clock clock) {
            if (capacity < 1) {
                throw new IllegalArgumentException("Rate limit capacity must be positive");
            }
            if (window.isZero() || window.isNegative()) {
                throw new IllegalArgumentException("Rate limit window must be positive");
            }
            this.capacity = capacity;
            this.windowMillis = window.toMillis();
            this.clock = clock;
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 20;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            if (!shouldRateLimit(exchange)) {
                return chain.filter(exchange);
            }

            long nowMillis = clock.millis();
            cleanupExpiredCounters(nowMillis);
            RateLimitDecision decision = counters.computeIfAbsent(resolveClientKey(exchange), key -> new WindowCounter(nowMillis))
                    .tryConsume(nowMillis, windowMillis, capacity);
            writeRateLimitHeaders(exchange, decision);

            if (!decision.allowed()) {
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().set(HttpHeaders.RETRY_AFTER, retryAfterSeconds(nowMillis, decision));
                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        }

        private boolean shouldRateLimit(ServerWebExchange exchange) {
            String path = exchange.getRequest().getPath().pathWithinApplication().value();
            return exchange.getRequest().getMethod() != HttpMethod.OPTIONS && path.startsWith("/api/");
        }

        private String resolveClientKey(ServerWebExchange exchange) {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null && remoteAddress.getAddress() != null) {
                return remoteAddress.getAddress().getHostAddress();
            }
            return UNKNOWN_CLIENT;
        }

        private void writeRateLimitHeaders(ServerWebExchange exchange, RateLimitDecision decision) {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set(RATE_LIMIT_LIMIT, Integer.toString(capacity));
            headers.set(RATE_LIMIT_REMAINING, Integer.toString(decision.remaining()));
            headers.set(RATE_LIMIT_RESET, Long.toString(decision.resetAt().getEpochSecond()));
        }

        private String retryAfterSeconds(long nowMillis, RateLimitDecision decision) {
            long remainingMillis = Math.max(0L, decision.resetAt().toEpochMilli() - nowMillis);
            long seconds = Math.max(1L, (long) Math.ceil(remainingMillis / 1000.0));
            return Long.toString(seconds);
        }

        private void cleanupExpiredCounters(long nowMillis) {
            counters.entrySet().removeIf(entry -> entry.getValue().expiredBefore(nowMillis, windowMillis));
        }
    }

    private static final class WindowCounter {

        private long windowStartMillis;
        private int requestCount;

        private WindowCounter(long windowStartMillis) {
            this.windowStartMillis = windowStartMillis;
        }

        private synchronized RateLimitDecision tryConsume(long nowMillis, long windowMillis, int capacity) {
            if (nowMillis - windowStartMillis >= windowMillis) {
                windowStartMillis = nowMillis;
                requestCount = 0;
            }

            Instant resetAt = Instant.ofEpochMilli(windowStartMillis + windowMillis);
            if (requestCount >= capacity) {
                return new RateLimitDecision(false, 0, resetAt);
            }

            requestCount++;
            return new RateLimitDecision(true, capacity - requestCount, resetAt);
        }

        private synchronized boolean expiredBefore(long nowMillis, long windowMillis) {
            return nowMillis - windowStartMillis >= windowMillis * 2;
        }
    }

    private record RateLimitDecision(boolean allowed, int remaining, Instant resetAt) {
    }
}
