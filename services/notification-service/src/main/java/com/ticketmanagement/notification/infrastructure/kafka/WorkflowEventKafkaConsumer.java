package com.ticketmanagement.notification.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ticketmanagement.notification.application.WorkflowEventNotificationService;

@Component
@RequiredArgsConstructor
public class WorkflowEventKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final WorkflowEventNotificationService notificationService;

    // Kafka'dan gelen workflow event mesajini idempotent notification islemine aktarir.
    @KafkaListener(
            topics = "${app.kafka.topics.workflow-events:workflow.events.v1}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public boolean handleWorkflowEvent(String message) {
        ConsumedEvent event = deserialize(message);
        return notificationService.handleWorkflowEvent(event);
    }

    // Kafka mesaj JSON'unu ortak event envelope alanlarina parse eder.
    private ConsumedEvent deserialize(String message) {
        try {
            return objectMapper.readValue(message, ConsumedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid workflow event message", exception);
        }
    }
}
