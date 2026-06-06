package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventType;
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.notification.application.EmailDeliveryService;
import com.ticketmanagement.notification.domain.EmailDeliveryStatus;
import com.ticketmanagement.notification.domain.EmailTemplateKey;
import com.ticketmanagement.notification.infrastructure.kafka.TicketEventKafkaConsumer;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryEntity;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryJpaRepository;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class NotificationEmailPipelineIntegrationTests {

    private static final int MAILPIT_SMTP_PORT = 1025;
    private static final int MAILPIT_HTTP_PORT = 8025;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("notification_app")
            .withPassword("notification_dev_password")
            .withInitScript("testdb/init-notification-schema.sql");

    @Container
    static GenericContainer<?> mailpit = new GenericContainer<>("axllent/mailpit:v1.22")
            .withExposedPorts(MAILPIT_SMTP_PORT, MAILPIT_HTTP_PORT);

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailDeliveryService emailDeliveryService;

    @Autowired
    private EmailDeliveryJpaRepository emailDeliveryRepository;

    @Autowired
    private TicketEventKafkaConsumer ticketEventKafkaConsumer;

    @DynamicPropertySource
    static void configureMailpit(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", mailpit::getHost);
        registry.add("spring.mail.port", () -> mailpit.getMappedPort(MAILPIT_SMTP_PORT));
        registry.add("management.health.mail.enabled", () -> false);
        registry.add("app.email.delivery.retry.enabled", () -> false);
        registry.add("app.email.delivery.retry.backoff", () -> "0");
    }

    @BeforeEach
    void cleanNotificationData() throws Exception {
        jdbcTemplate.update("delete from notification_schema.email_deliveries");
        jdbcTemplate.update("delete from notification_schema.notifications");
        jdbcTemplate.update("delete from notification_schema.processed_events");
        deleteMailpitMessages();
    }

    @Test
    void emailPipelineSendsRenderedTemplateToMailpitOnce() throws Exception {
        UUID sourceEventId = UUID.randomUUID();

        boolean firstEnqueue = emailDeliveryService.enqueueDelivery(
                sourceEventId,
                "customer@example.com",
                EmailTemplateKey.TICKET_CREATED,
                templateModel());
        boolean duplicateEnqueue = emailDeliveryService.enqueueDelivery(
                sourceEventId,
                "customer@example.com",
                EmailTemplateKey.TICKET_CREATED,
                templateModel());
        int firstRetryRun = emailDeliveryService.processDueDeliveries();
        int secondRetryRun = emailDeliveryService.processDueDeliveries();

        JsonNode latestMessage = waitForLatestMailpitMessage();
        EmailDeliveryEntity delivery = emailDeliveryRepository.findAll().getFirst();
        assertThat(firstEnqueue).isTrue();
        assertThat(duplicateEnqueue).isFalse();
        assertThat(firstRetryRun).isEqualTo(1);
        assertThat(secondRetryRun).isZero();
        assertThat(emailDeliveryRepository.findAll()).hasSize(1);
        assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
        assertThat(delivery.getRetryCount()).isZero();
        assertThat(delivery.getSentAt()).isNotNull();
        assertThat(latestMessage.path("Subject").asText()).isEqualTo("Ticket TCK-3201 was created");
        assertThat(latestMessage.path("To").toString()).contains("customer@example.com");
        assertThat(latestMessage.path("Text").asText()).contains("Ticket TCK-3201 was created.");
        assertThat(latestMessage.path("HTML").asText())
                .contains("Ticket")
                .contains("TCK-3201")
                .doesNotContain("<script>");
    }

    @Test
    void ticketCreatedNotificationEventEnqueuesAndSendsEmailToMailpit() throws Exception {
        UUID ticketId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        EventEnvelope<TicketCreatedPayload> envelope = EventEnvelope.of(
                EventType.TICKET_CREATED,
                customerId,
                ticketId,
                new TicketCreatedPayload(
                        ticketId,
                        "TCK-8801",
                        customerId,
                        UUID.randomUUID(),
                        "MOBILE_APP",
                        "Mobile App",
                        UUID.randomUUID(),
                        "FINANCE_OPERATIONS",
                        "Finance Operations",
                        "HIGH",
                        "NEW"));

        boolean processed = ticketEventKafkaConsumer.handleTicketEvent(objectMapper.writeValueAsString(envelope));
        int sentCount = emailDeliveryService.processDueDeliveries();

        JsonNode latestMessage = waitForLatestMailpitMessage();
        assertThat(processed).isTrue();
        assertThat(sentCount).isEqualTo(1);
        assertThat(emailDeliveryRepository.findAll()).hasSize(1);
        assertThat(latestMessage.path("Subject").asText()).isEqualTo("Ticket TCK-8801 was created");
        assertThat(latestMessage.path("To").toString()).contains("user-" + customerId.toString().substring(0, 8));
        assertThat(latestMessage.path("Text").asText()).contains("Ticket TCK-8801 was created.");
        assertThat(latestMessage.path("HTML").asText()).contains("Open ticket");
    }

    private static Map<String, Object> templateModel() {
        return Map.of(
                "customerName", "Customer <script>alert(1)</script>",
                "ticketNumber", "TCK-3201",
                "priority", "HIGH",
                "status", "NEW",
                "ticketUrl", "https://app.ticket.local/tickets/TCK-3201");
    }

    private JsonNode waitForLatestMailpitMessage() throws Exception {
        AssertionError lastFailure = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            HttpResponse<String> response = getMailpit("/api/v1/message/latest");
            if (response.statusCode() == 200) {
                return objectMapper.readTree(response.body());
            }
            lastFailure = new AssertionError("Mailpit latest message returned HTTP " + response.statusCode());
            Thread.sleep(250);
        }
        throw new AssertionError("Mailpit did not expose the sent message in time", lastFailure);
    }

    private void deleteMailpitMessages() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(mailpitUri("/api/v1/messages"))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString("{}"))
                .build();
        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getMailpit(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(mailpitUri(path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private static URI mailpitUri(String path) {
        return URI.create("http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(MAILPIT_HTTP_PORT) + path);
    }
}
