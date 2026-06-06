package com.ticketmanagement.notification.application;

import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ticketmanagement.notification.domain.EmailTemplateKey;
import com.ticketmanagement.notification.infrastructure.kafka.ConsumedEvent;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketEventNotificationService {

    static final String CONSUMER_NAME = "notification-service.ticket-events";
    private static final String TICKET_CREATED = "ticket.created";
    private static final String TICKET_EXTERNAL_COMMENT_ADDED = "ticket.external-comment-added";
    private static final String DEFAULT_COMMENT_PREVIEW = "New message available in the ticket timeline.";

    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final NotificationJpaRepository notificationRepository;
    private final NotificationLiveUpdateService notificationLiveUpdateService;
    private final EmailDeliveryService emailDeliveryService;
    private final NotificationRecipientDirectory recipientDirectory;

    @Value("${app.web.base-url:http://localhost:5173}")
    private String webBaseUrl;

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
        UUID ticketId = UUID.fromString(requiredPayloadText(event, "ticketId"));
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        NotificationEntity notification = notificationRepository.save(NotificationEntity.ticketCreated(
                UUID.randomUUID(),
                event.eventId(),
                customerId,
                ticketNumber));
        NotificationRecipientDirectory.NotificationRecipientProfile recipient = recipientDirectory.resolve(customerId);
        emailDeliveryService.enqueueDelivery(
                event.eventId(),
                recipient.email(),
                EmailTemplateKey.TICKET_CREATED,
                Map.of(
                        "customerName", recipient.displayName(),
                        "ticketNumber", ticketNumber,
                        "priority", requiredPayloadText(event, "priority"),
                        "status", requiredPayloadText(event, "status"),
                        "ticketUrl", ticketUrl("/tickets/", ticketId)));
        notificationLiveUpdateService.publishNotificationCreated(notification);
    }

    // External comment payload'indan karsi tarafa minimal UI notification kaydi olusturur.
    private void createExternalCommentNotification(ConsumedEvent event) {
        UUID recipientId = resolveExternalCommentRecipient(event);
        if (recipientId == null) {
            return;
        }
        UUID ticketId = UUID.fromString(requiredPayloadText(event, "ticketId"));
        UUID authorId = UUID.fromString(requiredPayloadText(event, "authorId"));
        UUID customerId = optionalPayloadUuid(event, "customerId");
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        NotificationEntity notification = notificationRepository.save(NotificationEntity.externalCommentAdded(
                UUID.randomUUID(),
                event.eventId(),
                recipientId,
                ticketNumber));
        NotificationRecipientDirectory.NotificationRecipientProfile recipient = recipientDirectory.resolve(recipientId);
        NotificationRecipientDirectory.NotificationRecipientProfile author = recipientDirectory.resolve(authorId);
        emailDeliveryService.enqueueDelivery(
                event.eventId(),
                recipient.email(),
                EmailTemplateKey.TICKET_EXTERNAL_COMMENT_ADDED,
                Map.of(
                        "recipientName", recipient.displayName(),
                        "ticketNumber", ticketNumber,
                        "commentAuthorName", author.displayName(),
                        "commentPreview", DEFAULT_COMMENT_PREVIEW,
                        "ticketUrl", ticketUrl(ticketRoutePrefix(recipientId, customerId), ticketId)));
        notificationLiveUpdateService.publishNotificationCreated(notification);
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

    // Alici tipine gore web route prefix'i secilir.
    private static String ticketRoutePrefix(UUID recipientId, UUID customerId) {
        if (customerId != null && customerId.equals(recipientId)) {
            return "/tickets/";
        }
        return "/agent/tickets/";
    }

    // E-posta template'i icin base URL ve ticket route'unu guvenli sekilde birlestirir.
    private String ticketUrl(String routePrefix, UUID ticketId) {
        String normalizedBaseUrl = webBaseUrl == null || webBaseUrl.isBlank()
                ? "http://localhost:5173"
                : webBaseUrl.trim();
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        return normalizedBaseUrl + routePrefix + ticketId;
    }
}
