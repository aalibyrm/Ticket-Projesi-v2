package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.event.workflow.SlaBreachedPayload;
import com.ticketmanagement.event.workflow.SlaRiskDetectedPayload;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.infrastructure.kafka.ConsumedEvent;

@Service
@RequiredArgsConstructor
public class ReportingWorkflowEventProjectionService {

    static final String CONSUMER_NAME = "reporting-service.workflow-events";
    private static final String SLA_RISK_DETECTED = "workflow.sla-risk-detected";
    private static final String SLA_BREACH_DETECTED = "workflow.sla-breach-detected";

    private final ObjectMapper objectMapper;
    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final ReportingProjectionService reportingProjectionService;

    // Workflow eventini idempotent sekilde reporting SLA projection guncellemesine cevirir.
    public boolean handleWorkflowEvent(ConsumedEvent event) {
        if (!SLA_RISK_DETECTED.equals(event.eventType()) && !SLA_BREACH_DETECTED.equals(event.eventType())) {
            return false;
        }
        return consumerIdempotencyService.processOnce(
                CONSUMER_NAME,
                event,
                () -> applyWorkflowEvent(event));
    }

    // Workflow event tipine gore SLA risk veya breach state'ini uygular.
    private void applyWorkflowEvent(ConsumedEvent event) {
        if (SLA_RISK_DETECTED.equals(event.eventType())) {
            applySlaRisk(event);
        } else if (SLA_BREACH_DETECTED.equals(event.eventType())) {
            applySlaBreach(event);
        }
    }

    // SLA risk event payload'indan ticket projection SLA state'ini AT_RISK yapar.
    private void applySlaRisk(ConsumedEvent event) {
        SlaRiskDetectedPayload payload = readPayload(event, SlaRiskDetectedPayload.class, SLA_RISK_DETECTED);
        reportingProjectionService.updateTicketSlaStatus(
                payload.ticketId(),
                ProjectionSlaStatus.AT_RISK,
                OffsetDateTime.ofInstant(payload.targetResolutionAt(), ZoneOffset.UTC),
                eventTime(event));
    }

    // SLA breach event payload'indan ticket projection SLA state'ini BREACHED yapar.
    private void applySlaBreach(ConsumedEvent event) {
        SlaBreachedPayload payload = readPayload(event, SlaBreachedPayload.class, SLA_BREACH_DETECTED);
        reportingProjectionService.updateTicketSlaStatus(
                payload.ticketId(),
                ProjectionSlaStatus.BREACHED,
                OffsetDateTime.ofInstant(payload.targetResolutionAt(), ZoneOffset.UTC),
                eventTime(event));
    }

    // Kafka envelope icindeki payload JSON'unu workflow event contract tipine cevirir.
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
}
