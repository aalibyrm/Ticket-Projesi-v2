package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventType;
import com.ticketmanagement.event.ticket.ExternalCommentAddedPayload;
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.notification.application.NotificationLiveUpdateService;
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

    @MockitoBean
    private NotificationLiveUpdateService notificationLiveUpdateService;

    @BeforeEach
    void cleanNotificationData() {
        reset(notificationLiveUpdateService);
        jdbcTemplate.update("delete from notification_schema.email_deliveries");
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
        assertThat(emailDeliveryCount()).isEqualTo(1);
        assertThat(emailTemplateKeyFor(envelope.eventId())).isEqualTo("ticket-created");
        assertThat(emailRecipientFor(envelope.eventId())).isEqualTo(fallbackEmail(customerId));
        assertThat(notificationCountFor(envelope.eventId())).isEqualTo(1);
        assertThat(notificationRecipientFor(envelope.eventId())).isEqualTo(customerId.toString());
        verify(notificationLiveUpdateService, times(1)).publishNotificationCreated(argThat(notification ->
                notification.getSourceEventId().equals(envelope.eventId())
                        && notification.getRecipientId().equals(customerId)));
    }

    @Test
    void externalCommentByAgentProducesCustomerNotificationOnce() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        EventEnvelope<ExternalCommentAddedPayload> envelope = EventEnvelope.of(
                EventType.TICKET_EXTERNAL_COMMENT_ADDED,
                agentId,
                ticketId,
                new ExternalCommentAddedPayload(
                        ticketId,
                        "TCK-2002",
                        UUID.randomUUID(),
                        agentId,
                        customerId,
                        agentId,
                        UUID.randomUUID()));
        String message = objectMapper.writeValueAsString(envelope);

        boolean firstDeliveryProcessed = ticketEventKafkaConsumer.handleTicketEvent(message);
        boolean duplicateDeliveryProcessed = ticketEventKafkaConsumer.handleTicketEvent(message);

        assertThat(firstDeliveryProcessed).isTrue();
        assertThat(duplicateDeliveryProcessed).isFalse();
        assertThat(processedEventCount()).isEqualTo(1);
        assertThat(notificationCount()).isEqualTo(1);
        assertThat(emailDeliveryCount()).isEqualTo(1);
        assertThat(emailTemplateKeyFor(envelope.eventId())).isEqualTo("ticket-external-comment-added");
        assertThat(emailRecipientFor(envelope.eventId())).isEqualTo(fallbackEmail(customerId));
        assertThat(notificationCountFor(envelope.eventId())).isEqualTo(1);
        assertThat(notificationRecipientFor(envelope.eventId())).isEqualTo(customerId.toString());
        assertThat(notificationTypeFor(envelope.eventId())).isEqualTo("TICKET_EXTERNAL_COMMENT_ADDED");
        verify(notificationLiveUpdateService, times(1)).publishNotificationCreated(argThat(notification ->
                notification.getSourceEventId().equals(envelope.eventId())
                        && notification.getRecipientId().equals(customerId)));
    }

    @Test
    void externalCommentByCustomerProducesAssignedAgentNotificationOnce() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        EventEnvelope<ExternalCommentAddedPayload> envelope = EventEnvelope.of(
                EventType.TICKET_EXTERNAL_COMMENT_ADDED,
                customerId,
                ticketId,
                new ExternalCommentAddedPayload(
                        ticketId,
                        "TCK-2003",
                        UUID.randomUUID(),
                        customerId,
                        customerId,
                        agentId,
                        UUID.randomUUID()));

        boolean processed = ticketEventKafkaConsumer.handleTicketEvent(objectMapper.writeValueAsString(envelope));

        assertThat(processed).isTrue();
        assertThat(notificationCount()).isEqualTo(1);
        assertThat(emailDeliveryCount()).isEqualTo(1);
        assertThat(emailTemplateKeyFor(envelope.eventId())).isEqualTo("ticket-external-comment-added");
        assertThat(emailRecipientFor(envelope.eventId())).isEqualTo(fallbackEmail(agentId));
        assertThat(notificationRecipientFor(envelope.eventId())).isEqualTo(agentId.toString());
        verify(notificationLiveUpdateService, times(1)).publishNotificationCreated(argThat(notification ->
                notification.getSourceEventId().equals(envelope.eventId())
                        && notification.getRecipientId().equals(agentId)));
    }

    private Integer processedEventCount() {
        return jdbcTemplate.queryForObject("select count(*) from notification_schema.processed_events", Integer.class);
    }

    private Integer notificationCount() {
        return jdbcTemplate.queryForObject("select count(*) from notification_schema.notifications", Integer.class);
    }

    private Integer emailDeliveryCount() {
        return jdbcTemplate.queryForObject("select count(*) from notification_schema.email_deliveries", Integer.class);
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

    private String notificationTypeFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select type from notification_schema.notifications where source_event_id = ?",
                String.class,
                eventId);
    }

    private String emailTemplateKeyFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select template_key from notification_schema.email_deliveries where source_event_id = ?",
                String.class,
                eventId);
    }

    private String emailRecipientFor(UUID eventId) {
        return jdbcTemplate.queryForObject(
                "select recipient_email from notification_schema.email_deliveries where source_event_id = ?",
                String.class,
                eventId);
    }

    private static String fallbackEmail(UUID actorId) {
        return "user-" + actorId.toString().substring(0, 8) + "@example.local";
    }
}
