package com.ticketmanagement.workflow.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ticketmanagement.workflow.application.TicketEventSlaService;

@Component
@RequiredArgsConstructor
public class TicketEventKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final TicketEventSlaService ticketEventSlaService;

    // Kafka'dan gelen ticket event mesajini idempotent SLA hesaplamasina aktarir.
    @KafkaListener(
            topics = "${app.kafka.topics.ticket-events:ticket.events.v1}",
            groupId = "${spring.kafka.consumer.group-id:workflow-sla-service}")
    public boolean handleTicketEvent(String message) {
        ConsumedEvent event = deserialize(message);
        return ticketEventSlaService.handleTicketEvent(event);
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
