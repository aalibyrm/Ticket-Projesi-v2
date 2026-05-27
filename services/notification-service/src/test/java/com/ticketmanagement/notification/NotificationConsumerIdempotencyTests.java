package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.notification.infrastructure.kafka.TicketEventKafkaConsumer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class NotificationConsumerIdempotencyTests {

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
    private TicketEventKafkaConsumer ticketEventKafkaConsumer;

    @BeforeEach
    void cleanNotificationData() {
        jdbcTemplate.update("delete from notification_schema.notifications");
        jdbcTemplate.update("delete from notification_schema.processed_events");
    }

    @Test
    void duplicateTicketCreatedEventProducesOneNotification() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        EventEnvelope<TicketCreatedPayload> envelope = EventEnvelope.of(
                EventType.TICKET_CREATED,
                customerId,
                ticketId,
                new TicketCreatedPayload(
                        ticketId,
                        "TCK-2001",
                        customerId,
                        UUID.randomUUID(),
                        "HIGH",
                        "NEW"));
        String message = objectMapper.writeValueAsString(envelope);

        boolean firstDeliveryProcessed = ticketEventKafkaConsumer.handleTicketEvent(message);
        boolean duplicateDeliveryProcessed = ticketEventKafkaConsumer.handleTicketEvent(message);

        assertThat(firstDeliveryProcessed).isTrue();
        assertThat(duplicateDeliveryProcessed).isFalse();
        assertThat(processedEventCount()).isEqualTo(1);
        assertThat(notificationCount()).isEqualTo(1);
        assertThat(notificationCountFor(envelope.eventId())).isEqualTo(1);
        assertThat(notificationRecipientFor(envelope.eventId())).isEqualTo(customerId.toString());
    }

    private Integer processedEventCount() {
        return jdbcTemplate.queryForObject("select count(*) from notification_schema.processed_events", Integer.class);
    }

    private Integer notificationCount() {
        return jdbcTemplate.queryForObject("select count(*) from notification_schema.notifications", Integer.class);
    }

    private Integer notificationCountFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from notification_schema.notifications where source_event_id = ?",
                Integer.class,
                eventId);
    }

    private String notificationRecipientFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select recipient_id::text from notification_schema.notifications where source_event_id = ?",
                String.class,
                eventId);
    }
}
