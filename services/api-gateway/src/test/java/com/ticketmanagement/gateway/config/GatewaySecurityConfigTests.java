package com.ticketmanagement.gateway.config;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = "app.security.jwt.enabled=true")
@AutoConfigureWebTestClient
class GatewaySecurityConfigTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void healthEndpointIsPublic() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void securedEndpointRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/tickets")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void securedEndpointAcceptsJwtAndRoutesToUnavailableDownstream() {
        webTestClient.mutateWith(mockJwt())
                .get()
                .uri("/api/tickets")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}

