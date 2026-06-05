package com.ticketmanagement.notification.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.event.EventType;
import com.ticketmanagement.notification.infrastructure.kafka.ConsumedEvent;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationJpaRepository;

@Service
@RequiredArgsConstructor
public class WorkflowEventNotificationService {

    static final String CONSUMER_NAME = "notification-service.workflow-events";

    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final NotificationJpaRepository notificationRepository;
    private final NotificationLiveUpdateService notificationLiveUpdateService;

    // Workflow eventini idempotent sekilde notification side effect'ine cevirir.
    public boolean handleWorkflowEvent(ConsumedEvent event) {
        if (!isSupportedSlaEvent(event.eventType())) {
            return false;
        }
        return consumerIdempotencyService.processOnce(
                CONSUMER_NAME,
                event,
                () -> createSlaNotification(event));
    }

    // SLA risk veya breach payload'indan minimal UI notification kaydi olusturur.
    private void createSlaNotification(ConsumedEvent event) {
        UUID recipientId = UUID.fromString(requiredPayloadText(event, "recipientId"));
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        NotificationEntity notification = EventType.WORKFLOW_SLA_RISK_DETECTED.eventName().equals(event.eventType())
                ? NotificationEntity.slaRisk(UUID.randomUUID(), event.eventId(), recipientId, ticketNumber)
                : NotificationEntity.slaBreach(UUID.randomUUID(), event.eventId(), recipientId, ticketNumber);
        NotificationEntity savedNotification = notificationRepository.save(notification);
        notificationLiveUpdateService.publishNotificationCreated(savedNotification);
    }

    // Event tipinin notification uretilen SLA eventi olup olmadigini dondurur.
    private static boolean isSupportedSlaEvent(String eventType) {
        return EventType.WORKFLOW_SLA_RISK_DETECTED.eventName().equals(eventType)
                || EventType.WORKFLOW_SLA_BREACH_DETECTED.eventName().equals(eventType);
    }

    // Zorunlu payload alanini guvenli sekilde okur.
    private static String requiredPayloadText(ConsumedEvent event, String fieldName) {
        if (event.payload() == null || event.payload().path(fieldName).isMissingNode()) {
            throw new IllegalArgumentException("Missing event payload field: " + fieldName);
        }
        String value = event.payload().path(fieldName).asText();
        if (value.isBlank()) {
            throw new IllegalArgumentException("Blank event payload field: " + fieldName);
        }
        return value;
    }
}
