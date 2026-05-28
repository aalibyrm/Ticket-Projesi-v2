package com.ticketmanagement.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventType;
import com.ticketmanagement.event.ticket.TicketAssignedPayload;
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.workflow.application.SlaDetectionService;
import com.ticketmanagement.workflow.infrastructure.kafka.TicketEventKafkaConsumer;
import com.ticketmanagement.workflow.infrastructure.outbox.OutboxPublisherService;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = SlaRiskBreachDetectionIntegrationTests.WORKFLOW_EVENTS_TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
class SlaRiskBreachDetectionIntegrationTests {

    static final String WORKFLOW_EVENTS_TOPIC = "workflow.events.v1";

    private static final Instant RISK_OPENED_AT = Instant.parse("2026-05-28T08:00:00Z");
    private static final Instant BREACH_OPENED_AT = Instant.parse("2026-05-28T06:00:00Z");
    private static final OffsetDateTime DETECTED_AT = OffsetDateTime.ofInstant(
            Instant.parse("2026-05-28T14:30:00Z"),
            ZoneOffset.UTC);

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("workflow_app")
            .withPassword("workflow_dev_password")
            .withInitScript("testdb/init-workflow-schema.sql");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private TicketEventKafkaConsumer ticketEventKafkaConsumer;

    @Autowired
    private SlaDetectionService slaDetectionService;

    @Autowired
    private OutboxPublisherService outboxPublisherService;

    @BeforeEach
    void cleanWorkflowData() {
        jdbcTemplate.update("delete from workflow_schema.outbox_events");
        jdbcTemplate.update("delete from workflow_schema.sla_ticket_states");
        jdbcTemplate.update("delete from workflow_schema.processed_events");
    }

    @Test
    void riskAndBreachDetectionCreateIdempotentOutboxEvents() throws Exception {
        UUID riskTicketId = consumeTicketCreated("TCK-RISK", "HIGH", RISK_OPENED_AT, UUID.randomUUID());
        UUID riskAssigneeId = UUID.randomUUID();
        consumeTicketAssigned(riskTicketId, riskAssigneeId);
        UUID breachCustomerId = UUID.randomUUID();
        UUID breachTicketId = consumeTicketCreated("TCK-BREACH", "HIGH", BREACH_OPENED_AT, breachCustomerId);

        int firstDetectionCount = slaDetectionService.detectDueSlaEvents(DETECTED_AT);
        int duplicateDetectionCount = slaDetectionService.detectDueSlaEvents(DETECTED_AT);

        JsonNode riskPayload = outboxPayloadFor(riskTicketId, EventType.WORKFLOW_SLA_RISK_DETECTED.eventName());
        JsonNode breachPayload = outboxPayloadFor(breachTicketId, EventType.WORKFLOW_SLA_BREACH_DETECTED.eventName());
        assertThat(firstDetectionCount).isEqualTo(2);
        assertThat(duplicateDetectionCount).isZero();
        assertThat(stateStatusFor(riskTicketId)).isEqualTo("AT_RISK");
        assertThat(stateStatusFor(breachTicketId)).isEqualTo("BREACHED");
        assertThat(riskPayload.path("recipientId").asText()).isEqualTo(riskAssigneeId.toString());
        assertThat(riskPayload.path("ticketNumber").asText()).isEqualTo("TCK-RISK");
        assertThat(breachPayload.path("recipientId").asText()).isEqualTo(breachCustomerId.toString());
        assertThat(breachPayload.path("ticketNumber").asText()).isEqualTo("TCK-BREACH");
        assertThat(outboxCount()).isEqualTo(2);
    }

    @Test
    void outboxPublisherDeliversSlaRiskEventToWorkflowTopic() throws Exception {
        UUID ticketId = consumeTicketCreated("TCK-PUBLISH", "HIGH", RISK_OPENED_AT, UUID.randomUUID());
        slaDetectionService.detectDueSlaEvents(DETECTED_AT);

        try (Consumer<String, String> consumer = createKafkaConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, WORKFLOW_EVENTS_TOPIC);

            int publishedCount = outboxPublisherService.publishPendingBatch();

            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                    consumer,
                    WORKFLOW_EVENTS_TOPIC,
                    Duration.ofSeconds(10));
            JsonNode envelope = objectMapper.readTree(record.value());
            assertThat(publishedCount).isEqualTo(1);
            assertThat(record.key()).isEqualTo(ticketId.toString());
            assertThat(envelope.path("eventType").asText())
                    .isEqualTo(EventType.WORKFLOW_SLA_RISK_DETECTED.eventName());
            assertThat(envelope.path("payload").path("ticketNumber").asText()).isEqualTo("TCK-PUBLISH");
            assertThat(outboxStatusFor(ticketId)).isEqualTo("PUBLISHED");
        }
    }

    @Test
    void priorityRiskWindowsAreInclusiveAndDoNotAlertEarly() throws Exception {
        OffsetDateTime detectedAt = OffsetDateTime.parse("2026-05-28T12:00:00Z");
        UUID highTicketId = consumeTicketCreated(
                "TCK-HIGH-RISK",
                "HIGH",
                detectedAt.minusHours(6).toInstant(),
                UUID.randomUUID());
        UUID mediumTicketId = consumeTicketCreated(
                "TCK-MED-RISK",
                "MEDIUM",
                detectedAt.minusHours(20).toInstant(),
                UUID.randomUUID());
        UUID lowTicketId = consumeTicketCreated(
                "TCK-LOW-RISK",
                "LOW",
                detectedAt.minusHours(60).toInstant(),
                UUID.randomUUID());
        UUID earlyMediumTicketId = consumeTicketCreated(
                "TCK-MED-EARLY",
                "MEDIUM",
                detectedAt.minusHours(19).plusMinutes(59).toInstant(),
                UUID.randomUUID());

        int detectedCount = slaDetectionService.detectDueSlaEvents(detectedAt);

        assertThat(detectedCount).isEqualTo(3);
        assertThat(stateStatusFor(highTicketId)).isEqualTo("AT_RISK");
        assertThat(stateStatusFor(mediumTicketId)).isEqualTo("AT_RISK");
        assertThat(stateStatusFor(lowTicketId)).isEqualTo("AT_RISK");
        assertThat(stateStatusFor(earlyMediumTicketId)).isEqualTo("ACTIVE");
        assertThat(outboxCount()).isEqualTo(3);
    }

    private UUID consumeTicketCreated(
            String ticketNumber,
            String priority,
            Instant openedAt,
            UUID customerId) throws Exception {
        UUID ticketId = UUID.randomUUID();
        ticketEventKafkaConsumer.handleTicketEvent(objectMapper.writeValueAsString(new EventEnvelope<>(
                UUID.randomUUID(),
                EventType.TICKET_CREATED.eventName(),
                EventType.TICKET_CREATED.version(),
                openedAt,
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
                        "NEW"))));
        return ticketId;
    }

    private void consumeTicketAssigned(UUID ticketId, UUID assigneeId) throws Exception {
        ticketEventKafkaConsumer.handleTicketEvent(objectMapper.writeValueAsString(new EventEnvelope<>(
                UUID.randomUUID(),
                EventType.TICKET_ASSIGNED.eventName(),
                EventType.TICKET_ASSIGNED.version(),
                RISK_OPENED_AT.plusSeconds(60),
                assigneeId,
                EventType.TICKET_ASSIGNED.aggregateType(),
                ticketId,
                null,
                new TicketAssignedPayload(
                        ticketId,
                        "TCK-RISK",
                        assigneeId,
                        UUID.randomUUID()))));
    }

    private Consumer<String, String> createKafkaConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "workflow-outbox-test-" + UUID.randomUUID(),
                "false",
                embeddedKafkaBroker);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer())
                .createConsumer();
    }

    private JsonNode outboxPayloadFor(UUID ticketId, String eventType) throws Exception {
        String payloadJson = jdbcTemplate.queryForObject(
                """
                        select payload::text
                        from workflow_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = ?
                        """,
                String.class,
                ticketId,
                eventType);
        return objectMapper.readTree(payloadJson);
    }

    private String stateStatusFor(UUID ticketId) {
        return jdbcTemplate.queryForObject(
                "select status from workflow_schema.sla_ticket_states where ticket_id = ?",
                String.class,
                ticketId);
    }

    private String outboxStatusFor(UUID ticketId) {
        return jdbcTemplate.queryForObject(
                "select status from workflow_schema.outbox_events where aggregate_id = ?",
                String.class,
                ticketId);
    }

    private Integer outboxCount() {
        return jdbcTemplate.queryForObject("select count(*) from workflow_schema.outbox_events", Integer.class);
    }
}
