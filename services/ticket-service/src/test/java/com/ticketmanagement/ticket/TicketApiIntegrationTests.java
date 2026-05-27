package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.AddWorklogRequest;
import com.ticketmanagement.ticket.api.dto.AssignTicketRequest;
import com.ticketmanagement.ticket.api.dto.ChangeTicketStatusRequest;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.ProductResponse;
import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.api.error.ApiErrorResponse;
import com.ticketmanagement.ticket.application.AttachmentLookupContext;
import com.ticketmanagement.ticket.application.TicketAttachmentPort;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxPublisherService;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OutboxPublisherService outboxPublisherService;

    @MockBean
    private TicketAttachmentPort ticketAttachmentPort;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void cleanTicketData() {
        jdbcTemplate.update("delete from ticket_schema.outbox_events");
        jdbcTemplate.update("delete from ticket_schema.ticket_comments");
        jdbcTemplate.update("delete from ticket_schema.ticket_worklogs");
        jdbcTemplate.update("delete from ticket_schema.tickets");
    }

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
        assertTicketCreatedOutboxEvent(created, customerId);

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

    @Test
    void agentActionsCreateVersionedOutboxEvents() {
        UUID customerId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketResponse> statusResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS), actorHeaders(agentId)),
                TicketResponse.class,
                created.id());

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody()).isNotNull();
        assertThat(statusResponse.getBody().status()).isEqualTo(TicketStatus.IN_PROGRESS);

        ResponseEntity<TicketResponse> assignmentResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/assignment",
                HttpMethod.PATCH,
                new HttpEntity<>(new AssignTicketRequest(agentId, teamId), actorHeaders(agentId)),
                TicketResponse.class,
                created.id());

        assertThat(assignmentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assignmentResponse.getBody()).isNotNull();
        assertThat(assignmentResponse.getBody().assigneeId()).isEqualTo(agentId);
        assertThat(assignmentResponse.getBody().assignedTeamId()).isEqualTo(teamId);

        ResponseEntity<TicketCommentResponse> commentResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("Customer-visible investigation update."), actorHeaders(agentId)),
                TicketCommentResponse.class,
                created.id());

        assertThat(commentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(commentResponse.getBody()).isNotNull();
        assertThat(commentResponse.getBody().ticketId()).isEqualTo(created.id());
        assertThat(commentResponse.getBody().visibility().name()).isEqualTo("EXTERNAL");

        ResponseEntity<TicketWorklogResponse> worklogResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/worklogs",
                HttpMethod.POST,
                new HttpEntity<>(new AddWorklogRequest(
                        LocalDate.parse("2026-05-27"),
                        45,
                        "Investigated dashboard authentication logs."),
                        actorHeaders(agentId)),
                TicketWorklogResponse.class,
                created.id());

        assertThat(worklogResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(worklogResponse.getBody()).isNotNull();
        assertThat(worklogResponse.getBody().ticketId()).isEqualTo(created.id());
        assertThat(worklogResponse.getBody().durationMinutes()).isEqualTo(45);

        assertThat(outboxCountFor(created.id())).isEqualTo(5);
        assertLifecycleEvent(created.id(), "ticket.created", "status", "NEW");
        assertLifecycleEvent(created.id(), "ticket.status-changed", "newStatus", "IN_PROGRESS");
        assertLifecycleEvent(created.id(), "ticket.assigned", "assigneeId", agentId.toString());
        assertLifecycleEvent(created.id(), "ticket.external-comment-added", "commentId", commentResponse.getBody().id().toString());
        assertLifecycleEvent(created.id(), "ticket.worklog-added", "worklogId", worklogResponse.getBody().id().toString());

        assertThat(outboxPayloadFor(created.id(), "ticket.external-comment-added"))
                .doesNotContain("Customer-visible investigation update.");
        assertThat(outboxPayloadFor(created.id(), "ticket.worklog-added"))
                .doesNotContain("Investigated dashboard authentication logs.");
    }

    @Test
    void publisherMarksOutboxEventPublishedAfterKafkaSend() {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        int publishedCount = outboxPublisherService.publishPendingBatch();

        assertThat(publishedCount).isEqualTo(1);
        Map<String, Object> outbox = outboxFor(created.id());
        assertThat(outbox.get("status")).isEqualTo("PUBLISHED");
        assertThat(outbox.get("retry_count")).isEqualTo(0);
        assertThat(outbox.get("published_at")).isNotNull();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, atLeastOnce()).send(
                eq("ticket.events.v1"),
                eq(created.id().toString()),
                messageCaptor.capture());
        assertThat(messageCaptor.getValue())
                .contains("\"eventType\":\"ticket.created\"")
                .contains("\"ticketId\":\"" + created.id())
                .doesNotContain(created.summary())
                .doesNotContain(created.description());
    }

    @Test
    void publisherRetriesFailedOutboxEventWithoutCreatingDuplicateRows() {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);
        CompletableFuture<SendResult<String, String>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(failedSend)
                .thenReturn(CompletableFuture.completedFuture(null));

        int firstPublishedCount = outboxPublisherService.publishPendingBatch();

        assertThat(firstPublishedCount).isZero();
        Map<String, Object> failedOutbox = outboxFor(created.id());
        assertThat(failedOutbox.get("status")).isEqualTo("FAILED");
        assertThat(failedOutbox.get("retry_count")).isEqualTo(1);
        assertThat(failedOutbox.get("next_attempt_at")).isNotNull();
        assertThat(outboxCountFor(created.id())).isEqualTo(1);

        jdbcTemplate.update(
                "update ticket_schema.outbox_events set next_attempt_at = now() - interval '1 second' where aggregate_id = ?",
                created.id());

        int secondPublishedCount = outboxPublisherService.publishPendingBatch();

        assertThat(secondPublishedCount).isEqualTo(1);
        Map<String, Object> publishedOutbox = outboxFor(created.id());
        assertThat(publishedOutbox.get("status")).isEqualTo("PUBLISHED");
        assertThat(publishedOutbox.get("retry_count")).isEqualTo(1);
        assertThat(outboxCountFor(created.id())).isEqualTo(1);
    }

    private TicketResponse createTicket(UUID customerId) {
        ProductResponse product = firstProduct();
        CreateTicketRequest request = new CreateTicketRequest(
                product.id(),
                "Cannot access dashboard",
                "Dashboard returns an error after login.",
                TicketPriority.HIGH);

        ResponseEntity<TicketResponse> response = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, actorHeaders(customerId)),
                TicketResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private ProductResponse firstProduct() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity("/api/products", ProductResponse[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        return response.getBody()[0];
    }

    private void assertTicketCreatedOutboxEvent(TicketResponse created, UUID actorId) {
        Map<String, Object> outbox = jdbcTemplate.queryForMap(
                """
                        select topic_name,
                               event_type,
                               event_version,
                               aggregate_type,
                               aggregate_id::text,
                               actor_id::text,
                               status,
                               retry_count,
                               payload ->> 'ticketId' as payload_ticket_id,
                               payload ->> 'ticketNumber' as payload_ticket_number,
                               payload ->> 'priority' as payload_priority,
                               payload ->> 'status' as payload_status,
                               payload::text as payload_json
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                        """,
                created.id());

        assertThat(outbox.get("topic_name")).isEqualTo("ticket.events.v1");
        assertThat(outbox.get("event_type")).isEqualTo("ticket.created");
        assertThat(outbox.get("event_version")).isEqualTo(1);
        assertThat(outbox.get("aggregate_type")).isEqualTo("ticket");
        assertThat(outbox.get("aggregate_id")).isEqualTo(created.id().toString());
        assertThat(outbox.get("actor_id")).isEqualTo(actorId.toString());
        assertThat(outbox.get("status")).isEqualTo("PENDING");
        assertThat(outbox.get("retry_count")).isEqualTo(0);
        assertThat(outbox.get("payload_ticket_id")).isEqualTo(created.id().toString());
        assertThat(outbox.get("payload_ticket_number")).isEqualTo(created.ticketNumber());
        assertThat(outbox.get("payload_priority")).isEqualTo("HIGH");
        assertThat(outbox.get("payload_status")).isEqualTo("NEW");
        assertThat(outbox.get("payload_json").toString()).doesNotContain(created.summary());
        assertThat(outbox.get("payload_json").toString()).doesNotContain(created.description());
    }

    private Map<String, Object> outboxFor(UUID ticketId) {
        return jdbcTemplate.queryForMap(
                """
                        select status,
                               retry_count,
                               next_attempt_at,
                               published_at
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                        """,
                ticketId);
    }

    private Integer outboxCountFor(UUID ticketId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.outbox_events where aggregate_id = ?",
                Integer.class,
                ticketId);
    }

    private void assertLifecycleEvent(UUID ticketId, String eventType, String payloadField, String payloadValue) {
        Map<String, Object> event = jdbcTemplate.queryForMap(
                """
                        select topic_name,
                               event_version,
                               status,
                               payload ->> ? as payload_value
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = ?
                        """,
                payloadField,
                ticketId,
                eventType);

        assertThat(event.get("topic_name")).isEqualTo("ticket.events.v1");
        assertThat(event.get("event_version")).isEqualTo(1);
        assertThat(event.get("status")).isEqualTo("PENDING");
        assertThat(event.get("payload_value")).isEqualTo(payloadValue);
    }

    private String outboxPayloadFor(UUID ticketId, String eventType) {
        return jdbcTemplate.queryForObject(
                """
                        select payload::text
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = ?
                        """,
                String.class,
                ticketId,
                eventType);
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
