package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.error.ApiErrorResponse;
import com.ticketmanagement.ticket.application.AttachmentLookupContext;
import com.ticketmanagement.ticket.application.TicketAttachmentPort;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.infrastructure.web.CorrelationIdFilter;

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

    @MockBean
    private TicketAttachmentPort ticketAttachmentPort;

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

        TicketAttachmentResponse attachment = attachmentFor(created.id());
        when(ticketAttachmentPort.listAttachments(eq(created.id()), any(AttachmentLookupContext.class)))
                .thenReturn(List.of(attachment));

        ResponseEntity<TicketResponse> ownDetail = restTemplate.exchange(
                "/api/tickets/{id}",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                TicketResponse.class,
                created.id());

        assertThat(ownDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ownDetail.getBody()).isNotNull();
        assertThat(ownDetail.getBody().id()).isEqualTo(created.id());
        assertThat(ownDetail.getBody().attachments()).containsExactly(attachment);

        ResponseEntity<ApiErrorResponse> otherDetail = restTemplate.exchange(
                "/api/tickets/{id}",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(otherCustomerId)),
                ApiErrorResponse.class,
                created.id());

        assertThat(otherDetail.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(otherDetail.getBody()).isNotNull();
        assertThat(otherDetail.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
        verify(ticketAttachmentPort, times(1)).listAttachments(eq(created.id()), any(AttachmentLookupContext.class));
    }

    @Test
    void invalidCreateRequestReturnsStandardValidationError() {
        UUID customerId = UUID.randomUUID();
        String correlationId = "validation-test-correlation";
        HttpHeaders headers = actorHeaders(customerId);
        headers.set(CorrelationIdFilter.HEADER_NAME, correlationId);
        CreateTicketRequest request = new CreateTicketRequest(null, "", "", null);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                ApiErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME)).isEqualTo(correlationId);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("VALIDATION_FAILED");
        assertThat(response.getBody().correlationId()).isEqualTo(correlationId);
        assertThat(response.getBody().validationErrors())
                .extracting(error -> error.field())
                .contains("productId", "summary", "description");
    }

    private ProductResponse firstProduct() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity("/api/products", ProductResponse[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        return response.getBody()[0];
    }

    private static TicketAttachmentResponse attachmentFor(UUID ticketId) {
        return new TicketAttachmentResponse(
                UUID.randomUUID(),
                ticketId,
                "error-log.txt",
                "text/plain",
                4096,
                "VALIDATED",
                "COMPLETED",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                OffsetDateTime.parse("2026-01-01T00:00:00Z"));
    }

    private static HttpHeaders actorHeaders(UUID actorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", actorId.toString());
        return headers;
    }
}
