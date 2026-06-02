package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.event.ticket.TicketAssignedPayload;
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.event.ticket.TicketStatusChangedPayload;
import com.ticketmanagement.event.ticket.WorklogAddedPayload;
import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;
import com.ticketmanagement.reporting.infrastructure.kafka.ConsumedEvent;

@Service
@RequiredArgsConstructor
public class ReportingTicketEventProjectionService {

    static final String CONSUMER_NAME = "reporting-service.ticket-events";
    private static final String TICKET_CREATED = "ticket.created";
    private static final String TICKET_ASSIGNED = "ticket.assigned";
    private static final String TICKET_STATUS_CHANGED = "ticket.status-changed";
    private static final String TICKET_WORKLOG_ADDED = "ticket.worklog-added";

    private final ObjectMapper objectMapper;
    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final ReportingProjectionService reportingProjectionService;

    // Ticket eventini idempotent sekilde reporting projection guncellemesine cevirir.
    public boolean handleTicketEvent(ConsumedEvent event) {
        if (!isSupportedTicketEvent(event.eventType())) {
            return false;
        }
        return consumerIdempotencyService.processOnce(
                CONSUMER_NAME,
                event,
                () -> applyTicketEvent(event));
    }

    // Ticket event tipine gore projection snapshot veya worklog projection gunceller.
    private void applyTicketEvent(ConsumedEvent event) {
        switch (event.eventType()) {
            case TICKET_CREATED -> createTicketProjection(event);
            case TICKET_ASSIGNED -> updateTicketAssignment(event);
            case TICKET_STATUS_CHANGED -> updateTicketStatus(event);
            case TICKET_WORKLOG_ADDED -> upsertWorklogProjection(event);
            default -> throw new IllegalArgumentException("Unsupported ticket event: " + event.eventType());
        }
    }

    // TicketCreated payload'indan ana ticket projection kaydi olusturur.
    private void createTicketProjection(ConsumedEvent event) {
        TicketCreatedPayload payload = readPayload(event, TicketCreatedPayload.class, TICKET_CREATED);
        OffsetDateTime occurredAt = eventTime(event);
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                payload.ticketId(),
                payload.ticketNumber(),
                payload.customerId(),
                payload.productId(),
                payload.topicCode(),
                payload.topicName(),
                payload.routedDepartmentId(),
                payload.routedDepartmentCode(),
                payload.routedDepartmentName(),
                ProjectionPriority.from(payload.priority()),
                ProjectionTicketStatus.from(payload.status()),
                null,
                null,
                occurredAt,
                occurredAt,
                null,
                null,
                null));
    }

    // TicketAssigned payload'indan assignee ve team alanlarini gunceller.
    private void updateTicketAssignment(ConsumedEvent event) {
        TicketAssignedPayload payload = readPayload(event, TicketAssignedPayload.class, TICKET_ASSIGNED);
        reportingProjectionService.updateTicketAssignment(
                payload.ticketId(),
                payload.assigneeId(),
                payload.assignedTeamId(),
                eventTime(event));
    }

    // TicketStatusChanged payload'indan status ve kapanis zamanini gunceller.
    private void updateTicketStatus(ConsumedEvent event) {
        TicketStatusChangedPayload payload = readPayload(event, TicketStatusChangedPayload.class, TICKET_STATUS_CHANGED);
        reportingProjectionService.updateTicketStatus(
                payload.ticketId(),
                ProjectionTicketStatus.from(payload.newStatus()),
                eventTime(event));
    }

    // WorklogAdded payload'indan agent bazli worklog projection kaydi uretir.
    private void upsertWorklogProjection(ConsumedEvent event) {
        WorklogAddedPayload payload = readPayload(event, WorklogAddedPayload.class, TICKET_WORKLOG_ADDED);
        reportingProjectionService.upsertAgentWorklogProjection(new AgentWorklogProjectionCommand(
                payload.worklogId(),
                payload.ticketId(),
                payload.ticketNumber(),
                payload.agentId(),
                payload.workDate(),
                payload.durationMinutes()));
    }

    // Kafka envelope icindeki payload JSON'unu event contract tipine cevirir.
    private <T> T readPayload(ConsumedEvent event, Class<T> payloadType, String eventType) {
        try {
            return objectMapper.treeToValue(event.payload(), payloadType);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid " + eventType + " payload", exception);
        }
    }

    // Event zamanini UTC OffsetDateTime olarak normalize eder.
    private static OffsetDateTime eventTime(ConsumedEvent event) {
        return event.occurredAt().withOffsetSameInstant(ZoneOffset.UTC);
    }

    private static boolean isSupportedTicketEvent(String eventType) {
        return TICKET_CREATED.equals(eventType)
                || TICKET_ASSIGNED.equals(eventType)
                || TICKET_STATUS_CHANGED.equals(eventType)
                || TICKET_WORKLOG_ADDED.equals(eventType);
    }
}
