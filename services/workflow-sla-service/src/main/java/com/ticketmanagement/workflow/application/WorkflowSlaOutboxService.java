package com.ticketmanagement.workflow.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventPayload;
import com.ticketmanagement.event.EventType;
import com.ticketmanagement.event.workflow.SlaBreachedPayload;
import com.ticketmanagement.event.workflow.SlaRiskDetectedPayload;
import com.ticketmanagement.workflow.config.SlaDetectionProperties;
import com.ticketmanagement.workflow.infrastructure.outbox.OutboxEventEntity;
import com.ticketmanagement.workflow.infrastructure.outbox.OutboxEventJpaRepository;
import com.ticketmanagement.workflow.infrastructure.persistence.SlaTicketStateEntity;

@Service
@RequiredArgsConstructor
public class WorkflowSlaOutboxService {

    private static final String RISK_REASON = "Target resolution deadline is approaching.";
    private static final String BREACH_REASON = "Target resolution deadline was missed.";

    private final ObjectMapper objectMapper;
    private final OutboxEventJpaRepository outboxEventRepository;
    private final SlaDetectionProperties detectionProperties;

    // SLA risk detection eventini mevcut transaction icinde outbox'a kaydeder.
    public void saveSlaRiskDetected(SlaTicketStateEntity state, OffsetDateTime detectedAt) {
        SlaRiskDetectedPayload payload = new SlaRiskDetectedPayload(
                state.getTicketId(),
                state.getTicketNumber(),
                state.alertRecipientId(),
                state.getPriority().name(),
                state.getTargetResolutionAt().toInstant(),
                detectedAt.toInstant(),
                RISK_REASON);
        savePending(EventType.WORKFLOW_SLA_RISK_DETECTED, state.getTicketId(), payload, detectedAt);
    }

    // SLA breach detection eventini mevcut transaction icinde outbox'a kaydeder.
    public void saveSlaBreached(SlaTicketStateEntity state, OffsetDateTime detectedAt) {
        SlaBreachedPayload payload = new SlaBreachedPayload(
                state.getTicketId(),
                state.getTicketNumber(),
                state.alertRecipientId(),
                state.getPriority().name(),
                state.getTargetResolutionAt().toInstant(),
                detectedAt.toInstant(),
                BREACH_REASON);
        savePending(EventType.WORKFLOW_SLA_BREACH_DETECTED, state.getTicketId(), payload, detectedAt);
    }

    // Hazirlanan SLA event envelope'unu pending outbox kaydi olarak saklar.
    private <T extends EventPayload> void savePending(
            EventType eventType,
            UUID aggregateId,
            T payload,
            OffsetDateTime detectedAt) {
        EventEnvelope<T> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                eventType.eventName(),
                eventType.version(),
                detectedAt.toInstant(),
                detectionProperties.getSystemActorId(),
                eventType.aggregateType(),
                aggregateId,
                null,
                payload);
        JsonNode payloadJson = objectMapper.valueToTree(payload);
        outboxEventRepository.save(OutboxEventEntity.pending(eventType.topic(), envelope, payloadJson));
    }
}
