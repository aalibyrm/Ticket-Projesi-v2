package com.ticketmanagement.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.OffsetDateTime;
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
import com.ticketmanagement.workflow.infrastructure.kafka.TicketEventKafkaConsumer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class SlaPolicyDeadlineIntegrationTests {

    private static final Instant OPENED_AT = Instant.parse("2026-05-28T08:00:00Z");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("workflow_app")
            .withPassword("workflow_dev_password")
            .withInitScript("testdb/init-workflow-schema.sql");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TicketEventKafkaConsumer ticketEventKafkaConsumer;

    @BeforeEach
    void cleanWorkflowData() {
        jdbcTemplate.update("delete from workflow_schema.sla_ticket_states");
        jdbcTemplate.update("delete from workflow_schema.processed_events");
    }

    @Test
    void lowMediumAndHighTicketsReceiveConfiguredResolutionDeadlines() throws Exception {
        UUID lowTicketId = consumeTicketCreated("TCK-LOW", "LOW");
        UUID mediumTicketId = consumeTicketCreated("TCK-MED", "MEDIUM");
        UUID highTicketId = consumeTicketCreated("TCK-HIGH", "HIGH");

        assertThat(targetResolutionAt(lowTicketId)).isEqualTo(OPENED_AT.plusSeconds(72 * 60 * 60));
        assertThat(targetResolutionAt(mediumTicketId)).isEqualTo(OPENED_AT.plusSeconds(24 * 60 * 60));
        assertThat(targetResolutionAt(highTicketId)).isEqualTo(OPENED_AT.plusSeconds(8 * 60 * 60));
        assertThat(slaStatusFor(highTicketId)).isEqualTo("ACTIVE");
    }

    @Test
    void duplicateTicketCreatedEventCreatesOneSlaState() throws Exception {
        EventEnvelope<TicketCreatedPayload> envelope = ticketCreatedEnvelope(UUID.randomUUID(), "TCK-DEDUP", "HIGH");
        String message = objectMapper.writeValueAsString(envelope);

        boolean firstDeliveryProcessed = ticketEventKafkaConsumer.handleTicketEvent(message);
        boolean duplicateDeliveryProcessed = ticketEventKafkaConsumer.handleTicketEvent(message);

        assertThat(firstDeliveryProcessed).isTrue();
        assertThat(duplicateDeliveryProcessed).isFalse();
        assertThat(processedEventCount()).isEqualTo(1);
        assertThat(slaStateCount()).isEqualTo(1);
    }

    private UUID consumeTicketCreated(String ticketNumber, String priority) throws Exception {
        UUID ticketId = UUID.randomUUID();
        ticketEventKafkaConsumer.handleTicketEvent(objectMapper.writeValueAsString(ticketCreatedEnvelope(
                ticketId,
                ticketNumber,
                priority)));
        return ticketId;
    }

    private EventEnvelope<TicketCreatedPayload> ticketCreatedEnvelope(UUID ticketId, String ticketNumber, String priority) {
        UUID customerId = UUID.randomUUID();
        return new EventEnvelope<>(
                UUID.randomUUID(),
                EventType.TICKET_CREATED.eventName(),
                EventType.TICKET_CREATED.version(),
                OPENED_AT,
                customerId,
                EventType.TICKET_CREATED.aggregateType(),
                ticketId,
                null,
                new TicketCreatedPayload(
                        ticketId,
                        ticketNumber,
                        customerId,
                        UUID.randomUUID(),
                        priority,
                        "NEW"));
    }

    private Instant targetResolutionAt(UUID ticketId) {
        OffsetDateTime target = jdbcTemplate.queryForObject(
                "select target_resolution_at from workflow_schema.sla_ticket_states where ticket_id = ?",
                OffsetDateTime.class,
                ticketId);
        return target.toInstant();
    }

    private String slaStatusFor(UUID ticketId) {
        return jdbcTemplate.queryForObject(
                "select status from workflow_schema.sla_ticket_states where ticket_id = ?",
                String.class,
                ticketId);
    }

    private Integer processedEventCount() {
        return jdbcTemplate.queryForObject("select count(*) from workflow_schema.processed_events", Integer.class);
    }

    private Integer slaStateCount() {
        return jdbcTemplate.queryForObject("select count(*) from workflow_schema.sla_ticket_states", Integer.class);
    }
}
