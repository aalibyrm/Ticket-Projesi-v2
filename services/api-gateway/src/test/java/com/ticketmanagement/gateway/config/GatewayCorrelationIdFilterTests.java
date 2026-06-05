package com.ticketmanagement.gateway.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

class GatewayCorrelationIdFilterTests {

    private final GatewayCorrelationIdFilter filter = new GatewayCorrelationIdFilter();

    @Test
    void keepsSafeCorrelationIdInResponseAndDownstreamRequest() {
        AtomicReference<String> downstreamCorrelationId = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/tickets")
                .header(GatewayCorrelationIdFilter.HEADER_NAME, "request-123")
                .build());

        filter.filter(exchange, captureCorrelationId(downstreamCorrelationId)).block();

        assertThat(exchange.getResponse().getHeaders().getFirst(GatewayCorrelationIdFilter.HEADER_NAME))
                .isEqualTo("request-123");
        assertThat(downstreamCorrelationId).hasValue("request-123");
    }

    @Test
    void replacesUnsafeCorrelationIdBeforeCallingDownstreamService() {
        AtomicReference<String> downstreamCorrelationId = new AtomicReference<>();
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/tickets")
                .header(GatewayCorrelationIdFilter.HEADER_NAME, "bad\nheader")
                .build());

        filter.filter(exchange, captureCorrelationId(downstreamCorrelationId)).block();

        String responseCorrelationId = exchange.getResponse().getHeaders()
                .getFirst(GatewayCorrelationIdFilter.HEADER_NAME);
        assertThat(responseCorrelationId).isNotBlank();
        assertThat(responseCorrelationId).isNotEqualTo("bad\nheader");
        assertThat(downstreamCorrelationId).hasValue(responseCorrelationId);
    }

    private static WebFilterChain captureCorrelationId(AtomicReference<String> downstreamCorrelationId) {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            downstreamCorrelationId.set(request.getHeaders().getFirst(GatewayCorrelationIdFilter.HEADER_NAME));
            return exchange.getResponse().setComplete();
        };
    }
}
