package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.ProductResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class TicketApiIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("ticket_app")
            .withPassword("ticket_dev_password")
            .withInitScript("testdb/init-ticket-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void customerCreatesAndListsOwnTickets() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        ProductResponse product = firstProduct();

        CreateTicketRequest request = new CreateTicketRequest(
                product.id(),
                "Cannot access dashboard",
                "Dashboard returns an error after login.",
                TicketPriority.HIGH);

        ResponseEntity<TicketResponse> createResponse = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, actorHeaders(customerId)),
                TicketResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TicketResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.customerId()).isEqualTo(customerId);
        assertThat(created.status()).isEqualTo(TicketStatus.NEW);
        assertThat(created.priority()).isEqualTo(TicketPriority.HIGH);
        assertThat(created.ticketNumber()).startsWith("TCK-");

        ResponseEntity<java.util.List<TicketResponse>> ownList = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                new ParameterizedTypeReference<>() {
                });

        assertThat(ownList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ownList.getBody()).extracting(TicketResponse::id).contains(created.id());

        ResponseEntity<java.util.List<TicketResponse>> otherList = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(otherCustomerId)),
                new ParameterizedTypeReference<>() {
                });

        assertThat(otherList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(otherList.getBody()).isEmpty();

        ResponseEntity<TicketResponse> ownDetail = restTemplate.exchange(
                "/api/tickets/{id}",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                TicketResponse.class,
                created.id());

        assertThat(ownDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ownDetail.getBody()).isNotNull();
        assertThat(ownDetail.getBody().id()).isEqualTo(created.id());

        ResponseEntity<String> otherDetail = restTemplate.exchange(
                "/api/tickets/{id}",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(otherCustomerId)),
                String.class,
                created.id());

        assertThat(otherDetail.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private ProductResponse firstProduct() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity("/api/products", ProductResponse[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        return response.getBody()[0];
    }

    private static HttpHeaders actorHeaders(UUID actorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", actorId.toString());
        return headers;
    }
}

