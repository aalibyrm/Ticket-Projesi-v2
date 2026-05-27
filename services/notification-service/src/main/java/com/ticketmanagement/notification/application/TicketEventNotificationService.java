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

    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final NotificationJpaRepository notificationRepository;

    // Ticket eventini idempotent sekilde notification side effect'ine cevirir.
    public boolean handleTicketEvent(ConsumedEvent event) {
        if (!TICKET_CREATED.equals(event.eventType())) {
            return false;
        }
        return consumerIdempotencyService.processOnce(
                CONSUMER_NAME,
                event,
                () -> createTicketCreatedNotification(event));
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
