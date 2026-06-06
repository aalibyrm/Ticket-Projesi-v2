package com.ticketmanagement.notification.application;

import java.util.Map;
import java.util.Locale;
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
    private static final UUID DEFAULT_SUPPORT_NOTIFICATION_RECIPIENT =
            UUID.fromString("80000000-0000-0000-0000-000000000002");
    private static final Map<String, UUID> TEAM_NOTIFICATION_RECIPIENTS = Map.of(
            "IDENTITY_OPERATIONS", UUID.fromString("40000000-0000-0000-0000-000000000001"),
            "PERMISSION_OPERATIONS", UUID.fromString("40000000-0000-0000-0000-000000000002"),
            "WEB_APP_SUPPORT", UUID.fromString("40000000-0000-0000-0000-000000000003"),
            "CORE_APP_SUPPORT", UUID.fromString("40000000-0000-0000-0000-000000000004"),
            "NETWORK_OPERATIONS", UUID.fromString("40000000-0000-0000-0000-000000000005"),
            "PLATFORM_OPERATIONS", UUID.fromString("40000000-0000-0000-0000-000000000006"),
            "BILLING_OPERATIONS", UUID.fromString("40000000-0000-0000-0000-000000000007"),
            "PAYMENT_OPERATIONS", UUID.fromString("40000000-0000-0000-0000-000000000008"));
    private static final String TICKET_CREATED = "ticket.created";
    private static final String TICKET_EXTERNAL_COMMENT_ADDED = "ticket.external-comment-added";
    private static final String TICKET_STATUS_CHANGED = "ticket.status-changed";
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
        if (TICKET_STATUS_CHANGED.equals(event.eventType())) {
            return consumerIdempotencyService.processOnce(
                    CONSUMER_NAME,
                    event,
                    () -> createStatusChangedNotification(event));
        }
        return false;
    }

    // TicketCreated payload'indan routed support actor icin UI notification ve e-posta olusturur.
    private void createTicketCreatedNotification(ConsumedEvent event) {
        UUID ticketId = UUID.fromString(requiredPayloadText(event, "ticketId"));
        UUID recipientId = resolveTicketCreatedRecipient(event);
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        NotificationEntity notification = notificationRepository.save(NotificationEntity.ticketCreated(
                UUID.randomUUID(),
                event.eventId(),
                recipientId,
                ticketId,
                ticketNumber));
        NotificationRecipientDirectory.NotificationRecipientProfile recipient = recipientDirectory.resolve(recipientId);
        emailDeliveryService.enqueueDelivery(
                event.eventId(),
                recipient.email(),
                EmailTemplateKey.TICKET_CREATED,
                Map.of(
                        "customerName", recipient.displayName(),
                        "ticketNumber", ticketNumber,
                        "priority", requiredPayloadText(event, "priority"),
                        "status", requiredPayloadText(event, "status"),
                        "ticketUrl", ticketUrl("/agent/tickets/", ticketId)));
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
                ticketId,
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

    // Status change payload'indan customer icin UI notification ve e-posta olusturur.
    private void createStatusChangedNotification(ConsumedEvent event) {
        UUID customerId = optionalPayloadUuid(event, "customerId");
        if (customerId == null) {
            return;
        }
        UUID ticketId = UUID.fromString(requiredPayloadText(event, "ticketId"));
        String ticketNumber = requiredPayloadText(event, "ticketNumber");
        String previousStatus = requiredPayloadText(event, "previousStatus");
        String newStatus = requiredPayloadText(event, "newStatus");
        NotificationEntity notification = notificationRepository.save(NotificationEntity.statusChanged(
                UUID.randomUUID(),
                event.eventId(),
                customerId,
                ticketId,
                ticketNumber,
                newStatus));
        NotificationRecipientDirectory.NotificationRecipientProfile recipient = recipientDirectory.resolve(customerId);
        emailDeliveryService.enqueueDelivery(
                event.eventId(),
                recipient.email(),
                EmailTemplateKey.TICKET_STATUS_CHANGED,
                Map.of(
                        "recipientName", recipient.displayName(),
                        "ticketNumber", ticketNumber,
                        "previousStatus", previousStatus,
                        "newStatus", newStatus,
                        "ticketUrl", ticketUrl("/tickets/", ticketId)));
        notificationLiveUpdateService.publishNotificationCreated(notification);
    }

    // Ticket created eventindeki explicit recipient veya team code fallback'i ile support alicisini cozer.
    private UUID resolveTicketCreatedRecipient(ConsumedEvent event) {
        UUID routedSupportActorId = optionalPayloadUuid(event, "routedSupportActorId");
        if (routedSupportActorId != null) {
            return routedSupportActorId;
        }
        String assignedTeamCode = optionalPayloadText(event, "assignedTeamCode");
        if (assignedTeamCode != null) {
            UUID teamRecipientId = TEAM_NOTIFICATION_RECIPIENTS.get(assignedTeamCode.toUpperCase(Locale.ROOT));
            if (teamRecipientId != null) {
                return teamRecipientId;
            }
        }
        return DEFAULT_SUPPORT_NOTIFICATION_RECIPIENT;
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

    // Opsiyonel text payload alanini normalize ederek okur.
    private static String optionalPayloadText(ConsumedEvent event, String fieldName) {
        if (event.payload() == null || event.payload().path(fieldName).isMissingNode()) {
            return null;
        }
        String value = event.payload().path(fieldName).asText();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
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
