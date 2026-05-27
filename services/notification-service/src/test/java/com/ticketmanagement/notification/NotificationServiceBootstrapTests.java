package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryEntity;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class NotificationServiceBootstrapTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("notification_app")
            .withPassword("notification_dev_password")
            .withInitScript("testdb/init-notification-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailDeliveryJpaRepository emailDeliveryRepository;

    @BeforeEach
    void cleanNotificationData() {
        jdbcTemplate.update("delete from notification_schema.email_deliveries");
        jdbcTemplate.update("delete from notification_schema.notifications");
        jdbcTemplate.update("delete from notification_schema.processed_events");
    }

    @Test
    void startsWithHealthEndpointAndNotificationSchemaMigrations() {
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).containsEntry("status", "UP");
        assertThat(tableExists("processed_events")).isEqualTo(1);
        assertThat(tableExists("notifications")).isEqualTo(1);
        assertThat(tableExists("email_deliveries")).isEqualTo(1);
        assertThat(serviceName()).isEqualTo("notification-service");
    }

    @Test
    void emailDeliveryEntityPersistsTemplateModel() {
        UUID sourceEventId = UUID.randomUUID();
        JsonNode templateModel = objectMapper.createObjectNode()
                .put("ticketNumber", "TCK-3001");
        EmailDeliveryEntity delivery = EmailDeliveryEntity.pending(
                UUID.randomUUID(),
                sourceEventId,
                "customer@example.com",
                "Ticket created",
                "ticket-created",
                templateModel);

        EmailDeliveryEntity saved = emailDeliveryRepository.saveAndFlush(delivery);

        assertThat(saved.getStatus().name()).isEqualTo("PENDING");
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(emailDeliveryRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(EmailDeliveryEntity::getSourceEventId)
                .isEqualTo(sourceEventId);
    }

    private Integer tableExists(String tableName) {
        return jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'notification_schema'
                          and table_name = ?
                        """,
                Integer.class,
                tableName);
    }

    private String serviceName() {
        return jdbcTemplate.queryForObject(
                """
                        select metadata_value
                        from notification_schema.service_metadata
                        where metadata_key = 'service_name'
                        """,
                String.class);
    }
}
