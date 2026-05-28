package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventType;
import com.ticketmanagement.event.workflow.SlaBreachedPayload;
import com.ticketmanagement.event.workflow.SlaRiskDetectedPayload;
import com.ticketmanagement.notification.infrastructure.kafka.WorkflowEventKafkaConsumer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class WorkflowSlaNotificationConsumerTests {

    private static final Instant DETECTED_AT = Instant.parse("2026-05-28T14:30:00Z");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("notification_app")
            .withPassword("notification_dev_password")
            .withInitScript("testdb/init-notification-schema.sql");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WorkflowEventKafkaConsumer workflowEventKafkaConsumer;

    @BeforeEach
    void cleanNotificationData() {
        jdbcTemplate.update("delete from notification_schema.email_deliveries");
        jdbcTemplate.update("delete from notification_schema.notifications");
        jdbcTemplate.update("delete from notification_schema.processed_events");
    }

    @Test
    void slaRiskAndBreachEventsProduceUiNotificationsIdempotently() throws Exception {
        UUID riskRecipientId = UUID.randomUUID();
        UUID breachRecipientId = UUID.randomUUID();
        EventEnvelope<SlaRiskDetectedPayload> riskEnvelope = slaRiskEnvelope(riskRecipientId);
        EventEnvelope<SlaBreachedPayload> breachEnvelope = slaBreachEnvelope(breachRecipientId);

        boolean firstRiskProcessed = workflowEventKafkaConsumer.handleWorkflowEvent(
                objectMapper.writeValueAsString(riskEnvelope));
        boolean duplicateRiskProcessed = workflowEventKafkaConsumer.handleWorkflowEvent(
                objectMapper.writeValueAsString(riskEnvelope));
        boolean breachProcessed = workflowEventKafkaConsumer.handleWorkflowEvent(
                objectMapper.writeValueAsString(breachEnvelope));

        assertThat(firstRiskProcessed).isTrue();
        assertThat(duplicateRiskProcessed).isFalse();
        assertThat(breachProcessed).isTrue();
        assertThat(notificationCount()).isEqualTo(2);
        assertThat(notificationTypeFor(riskEnvelope.eventId())).isEqualTo("SLA_RISK");
        assertThat(notificationTypeFor(breachEnvelope.eventId())).isEqualTo("SLA_BREACH");
        assertThat(notificationRecipientFor(riskEnvelope.eventId())).isEqualTo(riskRecipientId.toString());
        assertThat(notificationRecipientFor(breachEnvelope.eventId())).isEqualTo(breachRecipientId.toString());
    }

    private static EventEnvelope<SlaRiskDetectedPayload> slaRiskEnvelope(UUID recipientId) {
        UUID ticketId = UUID.randomUUID();
        return EventEnvelope.of(
                EventType.WORKFLOW_SLA_RISK_DETECTED,
                UUID.randomUUID(),
                ticketId,
                new SlaRiskDetectedPayload(
                        ticketId,
                        "TCK-RISK",
                        recipientId,
                        "HIGH",
                        DETECTED_AT.plusSeconds(7200),
                        DETECTED_AT,
                        "Target resolution deadline is approaching."));
    }

    private static EventEnvelope<SlaBreachedPayload> slaBreachEnvelope(UUID recipientId) {
        UUID ticketId = UUID.randomUUID();
        return EventEnvelope.of(
                EventType.WORKFLOW_SLA_BREACH_DETECTED,
                UUID.randomUUID(),
                ticketId,
                new SlaBreachedPayload(
                        ticketId,
                        "TCK-BREACH",
                        recipientId,
                        "HIGH",
                        DETECTED_AT.minusSeconds(60),
                        DETECTED_AT,
                        "Target resolution deadline was missed."));
    }

    private Integer notificationCount() {
        return jdbcTemplate.queryForObject("select count(*) from notification_schema.notifications", Integer.class);
    }

    private String notificationTypeFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select type from notification_schema.notifications where source_event_id = ?",
                String.class,
                eventId);
    }

    private String notificationRecipientFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select recipient_id::text from notification_schema.notifications where source_event_id = ?",
                String.class,
                eventId);
    }
}
