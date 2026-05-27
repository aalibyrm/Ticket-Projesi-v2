package com.ticketmanagement.notification.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ticketmanagement.notification.application.TicketEventNotificationService;

@Component
@RequiredArgsConstructor
public class TicketEventKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final TicketEventNotificationService notificationService;

    // Kafka'dan gelen ticket event mesajini idempotent notification islemine aktarir.
    @KafkaListener(
            topics = "${app.kafka.topics.ticket-events:ticket.events.v1}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public boolean handleTicketEvent(String message) {
        ConsumedEvent event = deserialize(message);
        return notificationService.handleTicketEvent(event);
    }

    // Kafka mesaj JSON'unu ortak event envelope alanlarina parse eder.
    private ConsumedEvent deserialize(String message) {
        try {
            return objectMapper.readValue(message, ConsumedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid ticket event message", exception);
        }
    }
}
