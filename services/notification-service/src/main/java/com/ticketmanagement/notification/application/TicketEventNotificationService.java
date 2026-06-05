package com.ticketmanagement.notification.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.notification.infrastructure.kafka.ConsumedEvent;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketEventNotificationService {

    static final String CONSUMER_NAME = "notification-service.ticket-events";
    private static final String TICKET_CREATED = "ticket.created";
    private static final String TICKET_EXTERNAL_COMMENT_ADDED = "ticket.external-comment-added";

    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final NotificationJpaRepository notificationRepository;

    // Ticket eventini idempotent sekilde notification side effect'ine cevirir.
    public boolean handleTicketEvent(ConsumedEvent event) {
        if (TICKET_CREATED.equals(event.eventType())) {
            return consumerIdempotencyService.processOnce(
                    CONSUMER_NAME,
                    event,
                    () -> createTicketCreatedNotification(event));
        }
        if (TICKET_EXTERNAL_COMMENT_ADDED.equals(event.eventType())) {
            return consumerIdempotencyService.processOnce(
                    CONSUMER_NAME,
                    event,
                    () -> createExternalCommentNotification(event));
        }
        return false;
    }

    // TicketCreated payload'indan minimal UI notification kaydi olusturur.
    private void createTicketCreatedNotification(ConsumedEvent event) {
        UUID customerId = UUID.fromString(requiredPayloadText(event, "customerId"));
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        notificationRepository.save(NotificationEntity.ticketCreated(
                UUID.randomUUID(),
                event.eventId(),
                customerId,
                ticketNumber));
    }

    // External comment payload'indan karsi tarafa minimal UI notification kaydi olusturur.
    private void createExternalCommentNotification(ConsumedEvent event) {
        UUID recipientId = resolveExternalCommentRecipient(event);
        if (recipientId == null) {
            return;
        }
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        notificationRepository.save(NotificationEntity.externalCommentAdded(
                UUID.randomUUID(),
                event.eventId(),
                recipientId,
                ticketNumber));
    }

    // Yorum sahibi musteri ise atanmis agent'i, support actor ise musteriyi hedefler.
    private UUID resolveExternalCommentRecipient(ConsumedEvent event) {
        UUID authorId = UUID.fromString(requiredPayloadText(event, "authorId"));
        UUID customerId = optionalPayloadUuid(event, "customerId");
        if (customerId != null && !customerId.equals(authorId)) {
            return customerId;
        }
        return optionalPayloadUuid(event, "assigneeId");
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

    // Opsiyonel UUID payload alanini bos veya eksikse null olarak dondurur.
    private static UUID optionalPayloadUuid(ConsumedEvent event, String fieldName) {
        if (event.payload() == null || event.payload().path(fieldName).isMissingNode()) {
            return null;
        }
        String value = event.payload().path(fieldName).asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return UUID.fromString(value);
    }
}
