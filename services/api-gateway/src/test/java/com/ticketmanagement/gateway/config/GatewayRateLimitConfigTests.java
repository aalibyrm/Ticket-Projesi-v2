package com.ticketmanagement.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = {
        "app.security.jwt.enabled=false",
        "app.rate-limit.capacity=1",
        "app.rate-limit.window=PT1M"
})
@AutoConfigureWebTestClient
class GatewayRateLimitConfigTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void throttlesApiRequestsAfterConfiguredCapacity() {
        webTestClient.get()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectHeader().valueEquals("X-RateLimit-Limit", "1")
                .expectHeader().valueEquals("X-RateLimit-Remaining", "0");

        webTestClient.get()
                .uri("/api/v1/products")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().exists(HttpHeaders.RETRY_AFTER)
                .expectHeader().valueEquals("X-RateLimit-Limit", "1")
                .expectHeader().valueEquals("X-RateLimit-Remaining", "0");
    }
}
