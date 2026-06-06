package com.ticketmanagement.gateway.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "app.security.jwt.enabled=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.resource.reactive.ReactiveOAuth2ResourceServerAutoConfiguration",
        "TICKET_SERVICE_URL=http://127.0.0.1:1",
        "WORKFLOW_SLA_SERVICE_URL=http://127.0.0.1:1",
        "REPORTING_SERVICE_URL=http://127.0.0.1:1"
})
@AutoConfigureWebTestClient
class GatewaySecurityConfigTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder reactiveJwtDecoder;

    @BeforeEach
    void setUpJwtDecoder() {
        when(reactiveJwtDecoder.decode(anyString()))
                .thenAnswer(invocation -> Mono.just(jwt(invocation.getArgument(0, String.class))));
    }

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
    void openApiDocsArePublicThroughGatewayAggregation() {
        webTestClient.get()
                .uri("/v3/api-docs/ticket-service")
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void securedEndpointRequiresAuthentication() {
        webTestClient.get()
                .uri("/api/v1/tickets")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void customerRoleCanReachCustomerTicketRoute() {
        webTestClient.get()
                .uri("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, bearer("customer-token"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void customerRoleCanReachTicketTopicRoute() {
        webTestClient.get()
                .uri("/api/v1/ticket-topics")
                .header(HttpHeaders.AUTHORIZATION, bearer("customer-token"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void agentRoleCanReachTicketOperationRoutes() {
        webTestClient.get()
                .uri("/api/v1/agent/tickets/queue")
                .header(HttpHeaders.AUTHORIZATION, bearer("agent-token"))
                .exchange()
                .expectStatus().is5xxServerError();

        webTestClient.get()
                .uri("/api/v1/workflows/tickets/00000000-0000-0000-0000-000000000001/transitions")
                .header(HttpHeaders.AUTHORIZATION, bearer("agent-token"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void agentRoleCanReachOrganizationRoutes() {
        webTestClient.get()
                .uri("/api/v1/organization/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer("agent-token"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void agentRoleCannotReachCustomerTicketOrManagerReportRoutes() {
        webTestClient.get()
                .uri("/api/v1/tickets")
                .header(HttpHeaders.AUTHORIZATION, bearer("agent-token"))
                .exchange()
                .expectStatus().isForbidden();

        webTestClient.get()
                .uri("/api/v1/reports/status-distribution")
                .header(HttpHeaders.AUTHORIZATION, bearer("agent-token"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void managerRoleCanReachReportAndSlaRoutes() {
        webTestClient.get()
                .uri("/api/v1/reports/status-distribution")
                .header(HttpHeaders.AUTHORIZATION, bearer("manager-token"))
                .exchange()
                .expectStatus().is5xxServerError();

        webTestClient.get()
                .uri("/api/v1/sla/compliance")
                .header(HttpHeaders.AUTHORIZATION, bearer("manager-token"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void managerRealmRoleClaimCanReachReportRoute() {
        webTestClient.get()
                .uri("/api/v1/reports/status-distribution")
                .header(HttpHeaders.AUTHORIZATION, bearer("manager-token"))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void customerRoleCannotReachManagerReportRoute() {
        webTestClient.get()
                .uri("/api/v1/reports/status-distribution")
                .header(HttpHeaders.AUTHORIZATION, bearer("customer-token"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void customerRoleCannotReachOrganizationRoutes() {
        webTestClient.get()
                .uri("/api/v1/organization/teams")
                .header(HttpHeaders.AUTHORIZATION, bearer("customer-token"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void managerRoleCannotReachAgentOperationRoute() {
        webTestClient.get()
                .uri("/api/v1/agent/tickets/queue")
                .header(HttpHeaders.AUTHORIZATION, bearer("manager-token"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void legacyUnversionedApiRoutesRemainProtectedDuringMigration() {
        webTestClient.get()
                .uri("/api/tickets")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static Jwt jwt(String token) {
        String role = switch (token) {
            case "customer-token" -> "CUSTOMER";
            case "agent-token" -> "AGENT";
            case "manager-token" -> "MANAGER";
            default -> "UNKNOWN";
        };

        return new Jwt(
                token,
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", token, "realm_access", Map.of("roles", List.of(role))));
    }
}
