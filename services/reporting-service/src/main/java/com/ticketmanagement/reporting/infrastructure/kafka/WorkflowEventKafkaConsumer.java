package com.ticketmanagement.reporting.infrastructure.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.ticketmanagement.reporting.application.ReportingWorkflowEventProjectionService;

@Component
@RequiredArgsConstructor
public class WorkflowEventKafkaConsumer {

    private final ObjectMapper objectMapper;
    private final ReportingWorkflowEventProjectionService reportingWorkflowEventProjectionService;

    // Kafka'dan gelen workflow event mesajini idempotent reporting projection islemine aktarir.
    @KafkaListener(
            topics = "${app.kafka.topics.workflow-events:workflow.events.v1}",
            groupId = "${spring.kafka.consumer.group-id:reporting-service}")
    public boolean handleWorkflowEvent(String message) {
        ConsumedEvent event = deserialize(message);
        return reportingWorkflowEventProjectionService.handleWorkflowEvent(event);
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
