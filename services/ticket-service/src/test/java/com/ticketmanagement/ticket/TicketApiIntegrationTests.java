package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.ticketmanagement.ticket.api.dto.AddInternalNoteRequest;
import com.ticketmanagement.ticket.api.dto.AddWorklogRequest;
import com.ticketmanagement.ticket.api.dto.AssignTicketRequest;
import com.ticketmanagement.ticket.api.dto.ChangeTicketStatusRequest;
import com.ticketmanagement.ticket.api.dto.ConversationReadStateResponse;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.ProductResponse;
import com.ticketmanagement.ticket.api.dto.TicketAgentSummaryResponse;
import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.api.error.ApiErrorResponse;
import com.ticketmanagement.ticket.application.AgentSummaryLookupPort;
import com.ticketmanagement.ticket.application.AgentSummaryMetrics;
import com.ticketmanagement.ticket.application.AttachmentLookupContext;
import com.ticketmanagement.ticket.application.TicketAttachmentPort;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxPublisherService;
import com.ticketmanagement.ticket.infrastructure.web.CorrelationIdFilter;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class TicketApiIntegrationTests {

    private static final String DEFAULT_TOPIC_CODE = "WEB_PORTAL_BUG";
    private static final UUID WEB_APP_SUPPORT_TEAM_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final UUID WEB_APP_SUPPORT_LEAD_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID WEB_APP_SUPPORT_MEMBER_ID = UUID.fromString("40000000-0000-0000-0000-000000000003");
    private static final UUID APPLICATION_SUPPORT_DEPARTMENT_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");

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
    private AgentSummaryLookupPort agentSummaryLookupPort;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void cleanTicketData() {
        jdbcTemplate.update("delete from ticket_schema.outbox_events");
        jdbcTemplate.update("delete from ticket_schema.ticket_conversation_reads");
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
                DEFAULT_TOPIC_CODE,
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
        assertThat(created.topicCode()).isEqualTo(DEFAULT_TOPIC_CODE);
        assertThat(created.topicName()).isEqualTo("Web Portal Bug");
        assertThat(created.routedDepartmentId()).isEqualTo(APPLICATION_SUPPORT_DEPARTMENT_ID);
        assertThat(created.routedDepartmentCode()).isEqualTo("APPLICATION_SUPPORT");
        assertThat(created.assignedTeamId()).isEqualTo(WEB_APP_SUPPORT_TEAM_ID);
        assertThat(created.assigneeId()).isNull();
        assertThat(created.ticketNumber()).startsWith("TCK-");
        assertTicketCreatedOutboxEvent(created, customerId);
        assertLifecycleEvent(created.id(), "ticket.assigned", "assignedTeamId", WEB_APP_SUPPORT_TEAM_ID.toString());

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
    void customerCanRetrieveAssignedAgentSummaryForOwnTicketOnly() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);
        assignTicket(created.id(), WEB_APP_SUPPORT_MEMBER_ID, WEB_APP_SUPPORT_TEAM_ID, adminId);
        when(agentSummaryLookupPort.getAgentSummary(WEB_APP_SUPPORT_MEMBER_ID))
                .thenReturn(new AgentSummaryMetrics(
                        WEB_APP_SUPPORT_MEMBER_ID,
                        12,
                        9,
                        3,
                        new BigDecimal("75.00")));

        ResponseEntity<TicketAgentSummaryResponse> response = restTemplate.exchange(
                "/api/tickets/{id}/agent-summary",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                TicketAgentSummaryResponse.class,
                created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().assigned()).isTrue();
        assertThat(response.getBody().agentId()).isEqualTo(WEB_APP_SUPPORT_MEMBER_ID);
        assertThat(response.getBody().displayName()).isEqualTo("Web Agent");
        assertThat(response.getBody().email()).isEqualTo("agent.web@example.local");
        assertThat(response.getBody().assignedTeamId()).isEqualTo(WEB_APP_SUPPORT_TEAM_ID);
        assertThat(response.getBody().resolvedTicketCount()).isEqualTo(12);
        assertThat(response.getBody().slaMetTicketCount()).isEqualTo(9);
        assertThat(response.getBody().slaBreachedTicketCount()).isEqualTo(3);
        assertThat(response.getBody().slaCompliancePercentage()).isEqualByComparingTo("75.00");
        assertThat(response.getBody().metricsAvailable()).isTrue();

        ResponseEntity<ApiErrorResponse> otherCustomerResponse = restTemplate.exchange(
                "/api/tickets/{id}/agent-summary",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(otherCustomerId)),
                ApiErrorResponse.class,
                created.id());

        assertThat(otherCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(otherCustomerResponse.getBody()).isNotNull();
        assertThat(otherCustomerResponse.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
        verify(agentSummaryLookupPort, times(1)).getAgentSummary(WEB_APP_SUPPORT_MEMBER_ID);
    }

    @Test
    void assignedAgentSummaryStillReturnsProfileWhenMetricsAreUnavailable() {
        UUID customerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);
        assignTicket(created.id(), WEB_APP_SUPPORT_MEMBER_ID, WEB_APP_SUPPORT_TEAM_ID, adminId);
        when(agentSummaryLookupPort.getAgentSummary(WEB_APP_SUPPORT_MEMBER_ID))
                .thenReturn(AgentSummaryMetrics.unavailable(WEB_APP_SUPPORT_MEMBER_ID));

        ResponseEntity<TicketAgentSummaryResponse> response = restTemplate.exchange(
                "/api/tickets/{id}/agent-summary",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                TicketAgentSummaryResponse.class,
                created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().assigned()).isTrue();
        assertThat(response.getBody().agentId()).isEqualTo(WEB_APP_SUPPORT_MEMBER_ID);
        assertThat(response.getBody().displayName()).isEqualTo("Web Agent");
        assertThat(response.getBody().email()).isEqualTo("agent.web@example.local");
        assertThat(response.getBody().assignedTeamId()).isEqualTo(WEB_APP_SUPPORT_TEAM_ID);
        assertThat(response.getBody().resolvedTicketCount()).isZero();
        assertThat(response.getBody().slaMetTicketCount()).isZero();
        assertThat(response.getBody().slaBreachedTicketCount()).isZero();
        assertThat(response.getBody().slaCompliancePercentage()).isEqualByComparingTo("0.00");
        assertThat(response.getBody().metricsAvailable()).isFalse();
    }

    @Test
    void unassignedTicketAgentSummaryDoesNotCallReportingService() {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketAgentSummaryResponse> response = restTemplate.exchange(
                "/api/tickets/{id}/agent-summary",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                TicketAgentSummaryResponse.class,
                created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().assigned()).isFalse();
        assertThat(response.getBody().agentId()).isNull();
        assertThat(response.getBody().displayName()).isNull();
        assertThat(response.getBody().assignedTeamId()).isEqualTo(WEB_APP_SUPPORT_TEAM_ID);
        assertThat(response.getBody().resolvedTicketCount()).isZero();
        assertThat(response.getBody().metricsAvailable()).isTrue();
        verifyNoInteractions(agentSummaryLookupPort);
    }

    @Test
    void customerAddsAndListsOwnExternalCommentsOnly() {
        UUID customerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketCommentResponse> commentResponse = restTemplate.exchange(
                "/api/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("I reproduced the problem after clearing cache."),
                        actorHeaders(customerId)),
                TicketCommentResponse.class,
                created.id());

        assertThat(commentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(commentResponse.getBody()).isNotNull();
        assertThat(commentResponse.getBody().ticketId()).isEqualTo(created.id());
        assertThat(commentResponse.getBody().authorId()).isEqualTo(customerId);
        assertThat(commentResponse.getBody().visibility().name()).isEqualTo("EXTERNAL");

        ResponseEntity<java.util.List<TicketCommentResponse>> commentsResponse = restTemplate.exchange(
                "/api/tickets/{id}/comments",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                new ParameterizedTypeReference<>() {
                },
                created.id());

        assertThat(commentsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(commentsResponse.getBody())
                .extracting(TicketCommentResponse::id)
                .containsExactly(commentResponse.getBody().id());
        assertThat(outboxPayloadFor(created.id(), "ticket.external-comment-added"))
                .doesNotContain("I reproduced the problem after clearing cache.");

        ResponseEntity<ApiErrorResponse> otherCustomerComment = restTemplate.exchange(
                "/api/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("Trying another customer's ticket."),
                        actorHeaders(otherCustomerId)),
                ApiErrorResponse.class,
                created.id());

        assertThat(otherCustomerComment.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(otherCustomerComment.getBody()).isNotNull();
        assertThat(otherCustomerComment.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void customerReadStateTracksAgentExternalMessages() {
        UUID customerId = UUID.randomUUID();
        UUID agentId = WEB_APP_SUPPORT_LEAD_ID;
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketCommentResponse> agentReply = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("We are checking the incident."),
                        actorHeaders(agentId)),
                TicketCommentResponse.class,
                created.id());

        assertThat(agentReply.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ConversationReadStateResponse> unreadState = restTemplate.exchange(
                "/api/tickets/{id}/comments/read-state",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                ConversationReadStateResponse.class,
                created.id());

        assertThat(unreadState.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(unreadState.getBody()).isNotNull();
        assertThat(unreadState.getBody().ticketId()).isEqualTo(created.id());
        assertThat(unreadState.getBody().unreadCount()).isEqualTo(1);
        assertThat(unreadState.getBody().lastReadAt()).isNull();

        ResponseEntity<ConversationReadStateResponse> readState = restTemplate.exchange(
                "/api/tickets/{id}/comments/read",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(customerId)),
                ConversationReadStateResponse.class,
                created.id());

        assertThat(readState.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readState.getBody()).isNotNull();
        assertThat(readState.getBody().unreadCount()).isZero();
        assertThat(readState.getBody().lastReadAt()).isNotNull();

        ResponseEntity<ConversationReadStateResponse> afterReadState = restTemplate.exchange(
                "/api/tickets/{id}/comments/read-state",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(customerId)),
                ConversationReadStateResponse.class,
                created.id());

        assertThat(afterReadState.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterReadState.getBody()).isNotNull();
        assertThat(afterReadState.getBody().unreadCount()).isZero();
    }

    @Test
    void supportReadStateTracksCustomerExternalMessages() {
        UUID customerId = UUID.randomUUID();
        UUID supportMemberId = WEB_APP_SUPPORT_MEMBER_ID;
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketCommentResponse> customerComment = restTemplate.exchange(
                "/api/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("The issue is still happening."),
                        actorHeaders(customerId)),
                TicketCommentResponse.class,
                created.id());

        assertThat(customerComment.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ConversationReadStateResponse> unreadState = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments/read-state",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(supportMemberId)),
                ConversationReadStateResponse.class,
                created.id());

        assertThat(unreadState.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(unreadState.getBody()).isNotNull();
        assertThat(unreadState.getBody().unreadCount()).isEqualTo(1);

        ResponseEntity<ConversationReadStateResponse> readState = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments/read",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(supportMemberId)),
                ConversationReadStateResponse.class,
                created.id());

        assertThat(readState.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readState.getBody()).isNotNull();
        assertThat(readState.getBody().unreadCount()).isZero();
        assertThat(readState.getBody().lastReadAt()).isNotNull();
    }

    @Test
    void invalidCreateRequestReturnsStandardValidationError() {
        UUID customerId = UUID.randomUUID();
        String correlationId = "validation-test-correlation";
        HttpHeaders headers = actorHeaders(customerId);
        headers.set(CorrelationIdFilter.HEADER_NAME, correlationId);
        CreateTicketRequest request = new CreateTicketRequest(null, "", "", "", null);

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
                .contains("productId", "topicCode", "summary", "description");
    }

    @Test
    void invalidTopicCodeIsRejected() {
        UUID customerId = UUID.randomUUID();
        ProductResponse product = firstProduct();
        CreateTicketRequest request = new CreateTicketRequest(
                product.id(),
                "UNKNOWN_TOPIC",
                "Cannot access dashboard",
                "Dashboard returns an error after login.",
                TicketPriority.MEDIUM);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, actorHeaders(customerId)),
                ApiErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void inactiveTopicCodeIsRejected() {
        insertInactiveTopicRoute();
        UUID customerId = UUID.randomUUID();
        ProductResponse product = firstProduct();
        CreateTicketRequest request = new CreateTicketRequest(
                product.id(),
                "LEGACY_TOPIC",
                "Cannot access legacy topic",
                "Legacy topic must not be routable.",
                TicketPriority.MEDIUM);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, actorHeaders(customerId)),
                ApiErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    @Test
    void createTicketIgnoresClientSuppliedAssignmentFields() {
        UUID customerId = UUID.randomUUID();
        ProductResponse product = firstProduct();
        UUID clientSuppliedTeamId = UUID.randomUUID();
        Map<String, Object> request = Map.of(
                "productId", product.id(),
                "topicCode", DEFAULT_TOPIC_CODE,
                "summary", "Cannot access dashboard",
                "description", "Dashboard returns an error after login.",
                "priority", "HIGH",
                "assigneeId", UUID.randomUUID(),
                "assignedTeamId", clientSuppliedTeamId);

        ResponseEntity<TicketResponse> response = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, actorHeaders(customerId)),
                TicketResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().assignedTeamId()).isEqualTo(WEB_APP_SUPPORT_TEAM_ID);
        assertThat(response.getBody().assignedTeamId()).isNotEqualTo(clientSuppliedTeamId);
        assertThat(response.getBody().assigneeId()).isNull();
    }

    @Test
    void agentActionsCreateVersionedOutboxEvents() {
        UUID customerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID agentId = WEB_APP_SUPPORT_MEMBER_ID;
        UUID teamId = WEB_APP_SUPPORT_TEAM_ID;
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketResponse> assignmentResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/assignment",
                HttpMethod.PATCH,
                new HttpEntity<>(new AssignTicketRequest(agentId, teamId), supportHeaders(adminId, "ADMIN")),
                TicketResponse.class,
                created.id());

        assertThat(assignmentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(assignmentResponse.getBody()).isNotNull();
        assertThat(assignmentResponse.getBody().assigneeId()).isEqualTo(agentId);
        assertThat(assignmentResponse.getBody().assignedTeamId()).isEqualTo(teamId);

        ResponseEntity<TicketResponse> statusResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS), actorHeaders(agentId)),
                TicketResponse.class,
                created.id());

        assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statusResponse.getBody()).isNotNull();
        assertThat(statusResponse.getBody().status()).isEqualTo(TicketStatus.IN_PROGRESS);

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

        assertThat(outboxCountFor(created.id())).isEqualTo(6);
        assertLifecycleEvent(created.id(), "ticket.created", "status", "NEW");
        assertLifecycleEvent(created.id(), "ticket.status-changed", "newStatus", "IN_PROGRESS");
        assertLifecycleEvent(created.id(), "ticket.status-changed", "customerId", customerId.toString());
        assertLifecycleEvent(created.id(), "ticket.assigned", "assigneeId", agentId.toString());
        assertLifecycleEvent(created.id(), "ticket.external-comment-added", "commentId", commentResponse.getBody().id().toString());
        assertLifecycleEvent(created.id(), "ticket.worklog-added", "worklogId", worklogResponse.getBody().id().toString());

        assertThat(outboxPayloadFor(created.id(), "ticket.external-comment-added"))
                .contains("\"customerId\": \"" + customerId)
                .contains("\"assigneeId\": \"" + agentId)
                .contains("\"assignedTeamId\": \"" + teamId)
                .doesNotContain("Customer-visible investigation update.");
        assertThat(outboxPayloadFor(created.id(), "ticket.worklog-added"))
                .doesNotContain("Investigated dashboard authentication logs.");
    }

    @Test
    void agentReadsAssignedQueueConversationWorklogsAndAttachments() {
        UUID customerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID agentId = WEB_APP_SUPPORT_LEAD_ID;
        UUID teamId = WEB_APP_SUPPORT_TEAM_ID;
        UUID teamMemberId = WEB_APP_SUPPORT_MEMBER_ID;
        TicketResponse created = createTicket(customerId);
        TicketAttachmentResponse attachment = attachmentFor(created.id());

        ResponseEntity<TicketResponse> assignmentResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/assignment",
                HttpMethod.PATCH,
                new HttpEntity<>(new AssignTicketRequest(agentId, teamId), supportHeaders(adminId, "ADMIN")),
                TicketResponse.class,
                created.id());
        assertThat(assignmentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        when(ticketAttachmentPort.listAttachments(eq(created.id()), any(AttachmentLookupContext.class)))
                .thenReturn(List.of(attachment));

        ResponseEntity<TicketCommentResponse> externalComment = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("Customer-visible response."), actorHeaders(agentId)),
                TicketCommentResponse.class,
                created.id());
        ResponseEntity<TicketCommentResponse> internalNote = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments/internal",
                HttpMethod.POST,
                new HttpEntity<>(new AddInternalNoteRequest("Internal triage note."), actorHeaders(agentId)),
                TicketCommentResponse.class,
                created.id());
        ResponseEntity<TicketWorklogResponse> worklog = restTemplate.exchange(
                "/api/agent/tickets/{id}/worklogs",
                HttpMethod.POST,
                new HttpEntity<>(new AddWorklogRequest(
                        LocalDate.parse("2026-05-27"),
                        30,
                        "Reviewed payment logs."),
                        actorHeaders(agentId)),
                TicketWorklogResponse.class,
                created.id());

        assertThat(externalComment.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(internalNote.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(worklog.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<java.util.List<TicketResponse>> queueResponse = restTemplate.exchange(
                "/api/agent/tickets",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(agentId)),
                new ParameterizedTypeReference<>() {
                });
        assertThat(queueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(queueResponse.getBody()).extracting(TicketResponse::id).contains(created.id());

        ResponseEntity<java.util.List<TicketResponse>> teamQueueResponse = restTemplate.exchange(
                "/api/agent/tickets",
                HttpMethod.GET,
                new HttpEntity<>(supportHeaders(teamMemberId, "AGENT")),
                new ParameterizedTypeReference<>() {
                });
        assertThat(teamQueueResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(teamQueueResponse.getBody()).extracting(TicketResponse::id).contains(created.id());

        ResponseEntity<TicketResponse> detailResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(agentId)),
                TicketResponse.class,
                created.id());
        assertThat(detailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detailResponse.getBody()).isNotNull();
        assertThat(detailResponse.getBody().attachments()).containsExactly(attachment);

        ResponseEntity<java.util.List<TicketCommentResponse>> commentsResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/comments",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(agentId)),
                new ParameterizedTypeReference<>() {
                },
                created.id());
        assertThat(commentsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(commentsResponse.getBody())
                .extracting(comment -> comment.visibility().name())
                .containsExactly("EXTERNAL", "INTERNAL");

        ResponseEntity<java.util.List<TicketWorklogResponse>> worklogsResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/worklogs",
                HttpMethod.GET,
                new HttpEntity<>(actorHeaders(agentId)),
                new ParameterizedTypeReference<>() {
                },
                created.id());
        assertThat(worklogsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(worklogsResponse.getBody()).extracting(TicketWorklogResponse::durationMinutes).containsExactly(30);
    }

    @Test
    void invalidStatusTransitionIsRejectedWithoutStatusEvent() {
        UUID customerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);

        ResponseEntity<TicketResponse> assignmentResponse = restTemplate.exchange(
                "/api/agent/tickets/{id}/assignment",
                HttpMethod.PATCH,
                new HttpEntity<>(new AssignTicketRequest(agentId, null), supportHeaders(adminId, "ADMIN")),
                TicketResponse.class,
                created.id());
        assertThat(assignmentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                "/api/agent/tickets/{id}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(new ChangeTicketStatusRequest(TicketStatus.CLOSED), actorHeaders(agentId)),
                ApiErrorResponse.class,
                created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_TICKET_OPERATION");
        assertThat(response.getBody().message()).contains("NEW -> CLOSED");
        assertThat(ticketStatusFor(created.id())).isEqualTo("NEW");
        assertThat(outboxCountFor(created.id())).isEqualTo(3);
    }

    @Test
    void waitingForCustomerCannotResumeBeforeCustomerReply() {
        UUID customerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID agentId = WEB_APP_SUPPORT_MEMBER_ID;
        TicketResponse created = createTicket(customerId);

        assignTicket(created.id(), agentId, WEB_APP_SUPPORT_TEAM_ID, adminId);
        ResponseEntity<TicketCommentResponse> staleComment = restTemplate.exchange(
                "/api/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("This is an earlier customer comment."), actorHeaders(customerId)),
                TicketCommentResponse.class,
                created.id());
        assertThat(staleComment.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        changeTicketStatus(created.id(), agentId, TicketStatus.IN_PROGRESS);
        changeTicketStatus(created.id(), agentId, TicketStatus.WAITING_FOR_CUSTOMER);

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                "/api/agent/tickets/{id}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS), actorHeaders(agentId)),
                ApiErrorResponse.class,
                created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("INVALID_TICKET_OPERATION");
        assertThat(response.getBody().message()).contains("Customer response is required");
        assertThat(ticketStatusFor(created.id())).isEqualTo(TicketStatus.WAITING_FOR_CUSTOMER.name());
    }

    @Test
    void waitingForCustomerCanResumeAfterCustomerReply() {
        UUID customerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID agentId = WEB_APP_SUPPORT_MEMBER_ID;
        TicketResponse created = createTicket(customerId);

        assignTicket(created.id(), agentId, WEB_APP_SUPPORT_TEAM_ID, adminId);
        changeTicketStatus(created.id(), agentId, TicketStatus.IN_PROGRESS);
        changeTicketStatus(created.id(), agentId, TicketStatus.WAITING_FOR_CUSTOMER);

        ResponseEntity<TicketCommentResponse> commentResponse = restTemplate.exchange(
                "/api/tickets/{id}/comments/external",
                HttpMethod.POST,
                new HttpEntity<>(new AddExternalCommentRequest("I added the missing payment details."), actorHeaders(customerId)),
                TicketCommentResponse.class,
                created.id());
        assertThat(commentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<TicketResponse> response = restTemplate.exchange(
                "/api/agent/tickets/{id}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS), actorHeaders(agentId)),
                TicketResponse.class,
                created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void publisherMarksOutboxEventPublishedAfterKafkaSend() {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        int publishedCount = outboxPublisherService.publishPendingBatch();

        assertThat(publishedCount).isEqualTo(2);
        Map<String, Object> outbox = outboxFor(created.id(), "ticket.created");
        assertThat(outbox.get("status")).isEqualTo("PUBLISHED");
        assertThat(outbox.get("retry_count")).isEqualTo(0);
        assertThat(outbox.get("published_at")).isNotNull();

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, atLeastOnce()).send(
                eq("ticket.events.v1"),
                eq(created.id().toString()),
                messageCaptor.capture());
        assertThat(messageCaptor.getAllValues())
                .anySatisfy(message -> assertThat(message)
                        .contains("\"eventType\":\"ticket.created\"")
                        .contains("\"ticketId\":\"" + created.id())
                        .doesNotContain(created.summary())
                        .doesNotContain(created.description()));
    }

    @Test
    void publisherRetriesFailedOutboxEventWithoutCreatingDuplicateRows() {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);
        CompletableFuture<SendResult<String, String>> failedSend = new CompletableFuture<>();
        failedSend.completeExceptionally(new RuntimeException("broker unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(failedSend)
                .thenReturn(failedSend)
                .thenReturn(CompletableFuture.completedFuture(null))
                .thenReturn(CompletableFuture.completedFuture(null));

        int firstPublishedCount = outboxPublisherService.publishPendingBatch();

        assertThat(firstPublishedCount).isZero();
        Map<String, Object> failedOutbox = outboxFor(created.id(), "ticket.created");
        assertThat(failedOutbox.get("status")).isEqualTo("FAILED");
        assertThat(failedOutbox.get("retry_count")).isEqualTo(1);
        assertThat(failedOutbox.get("next_attempt_at")).isNotNull();
        assertThat(outboxCountFor(created.id())).isEqualTo(2);

        jdbcTemplate.update(
                "update ticket_schema.outbox_events set next_attempt_at = now() - interval '1 second' where aggregate_id = ?",
                created.id());

        int secondPublishedCount = outboxPublisherService.publishPendingBatch();

        assertThat(secondPublishedCount).isEqualTo(2);
        Map<String, Object> publishedOutbox = outboxFor(created.id(), "ticket.created");
        assertThat(publishedOutbox.get("status")).isEqualTo("PUBLISHED");
        assertThat(publishedOutbox.get("retry_count")).isEqualTo(1);
        assertThat(outboxCountFor(created.id())).isEqualTo(2);
    }

    private TicketResponse createTicket(UUID customerId) {
        ProductResponse product = firstProduct();
        CreateTicketRequest request = new CreateTicketRequest(
                product.id(),
                DEFAULT_TOPIC_CODE,
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
                               payload ->> 'assignedTeamId' as payload_assigned_team_id,
                               payload ->> 'assignedTeamCode' as payload_assigned_team_code,
                               payload ->> 'routedSupportActorId' as payload_routed_support_actor_id,
                               payload::text as payload_json
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = 'ticket.created'
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
        assertThat(outbox.get("payload_assigned_team_id")).isEqualTo(WEB_APP_SUPPORT_TEAM_ID.toString());
        assertThat(outbox.get("payload_assigned_team_code")).isEqualTo("WEB_APP_SUPPORT");
        assertThat(outbox.get("payload_routed_support_actor_id")).isEqualTo(WEB_APP_SUPPORT_MEMBER_ID.toString());
        assertThat(outbox.get("payload_json").toString()).doesNotContain(created.summary());
        assertThat(outbox.get("payload_json").toString()).doesNotContain(created.description());
    }

    private Map<String, Object> outboxFor(UUID ticketId, String eventType) {
        return jdbcTemplate.queryForMap(
                """
                        select status,
                               retry_count,
                               next_attempt_at,
                               published_at
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = ?
                        """,
                ticketId,
                eventType);
    }

    private Integer outboxCountFor(UUID ticketId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.outbox_events where aggregate_id = ?",
                Integer.class,
                ticketId);
    }

    private String ticketStatusFor(UUID ticketId) {
        return jdbcTemplate.queryForObject(
                "select status from ticket_schema.tickets where id = ?",
                String.class,
                ticketId);
    }

    private TicketResponse assignTicket(UUID ticketId, UUID agentId, UUID teamId, UUID adminId) {
        ResponseEntity<TicketResponse> response = restTemplate.exchange(
                "/api/agent/tickets/{id}/assignment",
                HttpMethod.PATCH,
                new HttpEntity<>(new AssignTicketRequest(agentId, teamId), supportHeaders(adminId, "ADMIN")),
                TicketResponse.class,
                ticketId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private TicketResponse changeTicketStatus(UUID ticketId, UUID agentId, TicketStatus status) {
        ResponseEntity<TicketResponse> response = restTemplate.exchange(
                "/api/agent/tickets/{id}/status",
                HttpMethod.PATCH,
                new HttpEntity<>(new ChangeTicketStatusRequest(status), actorHeaders(agentId)),
                TicketResponse.class,
                ticketId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
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
                          and payload ->> ? = ?
                        """,
                payloadField,
                ticketId,
                eventType,
                payloadField,
                payloadValue);

        assertThat(event.get("topic_name")).isEqualTo("ticket.events.v1");
        assertThat(event.get("event_version")).isEqualTo(1);
        assertThat(event.get("status")).isEqualTo("PENDING");
        assertThat(event.get("payload_value")).isEqualTo(payloadValue);
    }

    private void insertInactiveTopicRoute() {
        jdbcTemplate.update(
                """
                        insert into ticket_schema.ticket_topics (id, code, name, description, active)
                        values (
                          '90000000-0000-0000-0000-000000000101',
                          'LEGACY_TOPIC',
                          'Legacy Topic',
                          'Inactive legacy topic.',
                          false
                        )
                        on conflict (code) do nothing
                        """);
        jdbcTemplate.update(
                """
                        insert into ticket_schema.ticket_routing_rules (
                          id,
                          topic_id,
                          department_id,
                          team_id,
                          active
                        )
                        values (
                          '90000000-0000-0000-0000-000000000102',
                          '90000000-0000-0000-0000-000000000101',
                          '10000000-0000-0000-0000-000000000002',
                          '20000000-0000-0000-0000-000000000003',
                          true
                        )
                        on conflict (topic_id) do nothing
                        """);
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

    private static HttpHeaders supportHeaders(UUID actorId, String... roles) {
        HttpHeaders headers = actorHeaders(actorId);
        headers.set("X-Actor-Roles", String.join(",", roles));
        return headers;
    }
}
