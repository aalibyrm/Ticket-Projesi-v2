package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.ProductResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.application.TicketAttachmentPort;
import com.ticketmanagement.ticket.application.TicketCommandService;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxPublisherService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        partitions = 1,
        topics = TicketOutboxReliabilityIntegrationTests.TICKET_EVENTS_TOPIC,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext
class TicketOutboxReliabilityIntegrationTests {

    static final String TICKET_EVENTS_TOPIC = "ticket.events.v1";
    private static final String DEFAULT_TOPIC_CODE = "WEB_PORTAL_BUG";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("ticket_app")
            .withPassword("ticket_dev_password")
            .withInitScript("testdb/init-ticket-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private OutboxPublisherService outboxPublisherService;

    @Autowired
    private TicketCommandService ticketCommandService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockBean
    private TicketAttachmentPort ticketAttachmentPort;

    @BeforeEach
    void cleanTicketData() {
        jdbcTemplate.update("delete from ticket_schema.outbox_events");
        jdbcTemplate.update("delete from ticket_schema.ticket_comments");
        jdbcTemplate.update("delete from ticket_schema.ticket_worklogs");
        jdbcTemplate.update("delete from ticket_schema.tickets");
    }

    @Test
    void committedTicketCreationPersistsPendingOutboxEvent() {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);

        Map<String, Object> outbox = outboxEnvelopeFor(created.id());

        assertThat(outbox.get("topic_name")).isEqualTo(TICKET_EVENTS_TOPIC);
        assertThat(outbox.get("event_type")).isEqualTo("ticket.created");
        assertThat(outbox.get("event_version")).isEqualTo(1);
        assertThat(outbox.get("aggregate_id")).isEqualTo(created.id().toString());
        assertThat(outbox.get("actor_id")).isEqualTo(customerId.toString());
        assertThat(outbox.get("status")).isEqualTo("PENDING");
        assertThat(outbox.get("retry_count")).isEqualTo(0);
        assertThat(outbox.get("payload_json").toString())
                .contains(created.ticketNumber())
                .doesNotContain(created.summary())
                .doesNotContain(created.description());
    }

    @Test
    void rolledBackTicketCreationDoesNotPersistTicketOrOutboxEvent() {
        UUID customerId = UUID.randomUUID();
        UUID productId = firstProductId();
        CreateTicketRequest request = new CreateTicketRequest(
                productId,
                DEFAULT_TOPIC_CODE,
                "Rollback test ticket",
                "This transaction must not leave ticket or outbox rows.",
                TicketPriority.HIGH);

        Throwable thrown = catchThrowable(() -> transactionTemplate.executeWithoutResult(status -> {
            ticketCommandService.createTicket(customerId, request);
            throw new IllegalStateException("force rollback");
        }));

        assertThat(thrown).isInstanceOf(IllegalStateException.class)
                .hasMessage("force rollback");
        assertThat(ticketCountFor(customerId)).isZero();
        assertThat(outboxCountForActor(customerId)).isZero();
    }

    @Test
    void publisherDeliversCommittedOutboxEventToKafkaAndMarksPublished() throws Exception {
        UUID customerId = UUID.randomUUID();
        TicketResponse created = createTicket(customerId);

        try (Consumer<String, String> consumer = createKafkaConsumer()) {
            embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TICKET_EVENTS_TOPIC);

            int publishedCount = outboxPublisherService.publishPendingBatch();

            assertThat(publishedCount).isEqualTo(2);
            ConsumerRecords<String, String> records = KafkaTestUtils.getRecords(
                    consumer,
                    Duration.ofSeconds(10));
            ConsumerRecord<String, String> record = null;
            for (ConsumerRecord<String, String> candidate : records.records(TICKET_EVENTS_TOPIC)) {
                if (candidate.value().contains("\"eventType\":\"ticket.created\"")) {
                    record = candidate;
                    break;
                }
            }

            assertThat(record).isNotNull();
            JsonNode envelope = objectMapper.readTree(record.value());
            assertThat(record.key()).isEqualTo(created.id().toString());
            assertThat(envelope.path("eventType").asText()).isEqualTo("ticket.created");
            assertThat(envelope.path("aggregateId").asText()).isEqualTo(created.id().toString());
            assertThat(envelope.path("actorId").asText()).isEqualTo(customerId.toString());
            assertThat(envelope.path("payload").path("ticketId").asText()).isEqualTo(created.id().toString());
            assertThat(envelope.path("payload").path("ticketNumber").asText()).isEqualTo(created.ticketNumber());
            assertThat(record.value())
                    .doesNotContain(created.summary())
                    .doesNotContain(created.description());
        }

        Map<String, Object> outbox = outboxStateFor(created.id(), "ticket.created");
        assertThat(outbox.get("status")).isEqualTo("PUBLISHED");
        assertThat(outbox.get("retry_count")).isEqualTo(0);
        assertThat(outbox.get("published_at")).isNotNull();
        assertThat(outbox.get("next_attempt_at")).isNull();
    }

    private Consumer<String, String> createKafkaConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "ticket-outbox-test-" + UUID.randomUUID(),
                "false",
                embeddedKafkaBroker);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer())
                .createConsumer();
    }

    private TicketResponse createTicket(UUID customerId) {
        ProductResponse product = firstProduct();
        CreateTicketRequest request = new CreateTicketRequest(
                product.id(),
                DEFAULT_TOPIC_CODE,
                "Cannot access dashboard",
                "Dashboard returns an error after login.",
                TicketPriority.HIGH);

        ResponseEntity<TicketResponse> response = restTemplate.exchange(
                "/api/tickets",
                HttpMethod.POST,
                new HttpEntity<>(request, actorHeaders(customerId)),
                TicketResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private ProductResponse firstProduct() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity("/api/products", ProductResponse[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotEmpty();
        return response.getBody()[0];
    }

    private UUID firstProductId() {
        return jdbcTemplate.queryForObject(
                "select id from ticket_schema.products where active = true order by code limit 1",
                UUID.class);
    }

    private Map<String, Object> outboxEnvelopeFor(UUID ticketId) {
        return jdbcTemplate.queryForMap(
                """
                        select topic_name,
                               event_type,
                               event_version,
                               aggregate_id::text,
                               actor_id::text,
                               status,
                               retry_count,
                               payload::text as payload_json
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = 'ticket.created'
                        """,
                ticketId);
    }

    private Map<String, Object> outboxStateFor(UUID ticketId, String eventType) {
        return jdbcTemplate.queryForMap(
                """
                        select status,
                               retry_count,
                               next_attempt_at,
                               published_at
                        from ticket_schema.outbox_events
                        where aggregate_id = ?
                          and event_type = ?
                        """,
                ticketId,
                eventType);
    }

    private Integer ticketCountFor(UUID customerId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.tickets where customer_id = ?",
                Integer.class,
                customerId);
    }

    private Integer outboxCountForActor(UUID actorId) {
        return jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.outbox_events where actor_id = ?",
                Integer.class,
                actorId);
    }

    private static HttpHeaders actorHeaders(UUID actorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", actorId.toString());
        return headers;
    }
}
