package com.ticketmanagement.gateway.config;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Frame-Options", "DENY")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Referrer-Policy", "no-referrer");
    }

    @Test
    void securedEndpointRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/tickets")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void customerRoleCanReachCustomerTicketRoute() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .get()
                .uri("/api/tickets")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void agentRoleCanReachTicketOperationRoutes() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_AGENT")))
                .get()
                .uri("/api/agent/tickets/queue")
                .exchange()
                .expectStatus().is5xxServerError();

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_AGENT")))
                .get()
                .uri("/api/workflows/tickets/00000000-0000-0000-0000-000000000001/transitions")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void agentRoleCannotReachCustomerTicketOrManagerReportRoutes() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_AGENT")))
                .get()
                .uri("/api/tickets")
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_AGENT")))
                .get()
                .uri("/api/reports/status-distribution")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void managerRoleCanReachReportAndSlaRoutes() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER")))
                .get()
                .uri("/api/reports/status-distribution")
                .exchange()
                .expectStatus().is5xxServerError();

        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER")))
                .get()
                .uri("/api/sla/compliance")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void managerRealmRoleClaimCanReachReportRoute() {
        webTestClient.mutateWith(mockJwt()
                        .jwt(token -> token.claim("realm_access", Map.of("roles", List.of("MANAGER"))))
                        .authorities(new GatewayJwtRealmRoleConverter()))
                .get()
                .uri("/api/reports/status-distribution")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void customerRoleCannotReachManagerReportRoute() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                .get()
                .uri("/api/reports/status-distribution")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void managerRoleCannotReachAgentOperationRoute() {
        webTestClient.mutateWith(mockJwt().authorities(new SimpleGrantedAuthority("ROLE_MANAGER")))
                .get()
                .uri("/api/agent/tickets/queue")
                .exchange()
                .expectStatus().isForbidden();
    }
}
