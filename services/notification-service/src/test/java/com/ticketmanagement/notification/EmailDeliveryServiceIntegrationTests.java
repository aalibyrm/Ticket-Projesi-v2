package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.notification.application.EmailDeliveryException;
import com.ticketmanagement.notification.application.EmailDeliveryService;
import com.ticketmanagement.notification.application.EmailMessage;
import com.ticketmanagement.notification.application.EmailSenderPort;
import com.ticketmanagement.notification.domain.EmailDeliveryStatus;
import com.ticketmanagement.notification.domain.EmailTemplateKey;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryEntity;
import com.ticketmanagement.notification.infrastructure.persistence.EmailDeliveryJpaRepository;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class EmailDeliveryServiceIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("notification_app")
            .withPassword("notification_dev_password")
            .withInitScript("testdb/init-notification-schema.sql");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailDeliveryService emailDeliveryService;

    @Autowired
    private EmailDeliveryJpaRepository emailDeliveryRepository;

    @Autowired
    private RecordingEmailSenderPort recordingEmailSender;

    @BeforeEach
    void cleanNotificationData() {
        jdbcTemplate.update("delete from notification_schema.email_deliveries");
        jdbcTemplate.update("delete from notification_schema.notifications");
        jdbcTemplate.update("delete from notification_schema.processed_events");
        recordingEmailSender.reset();
    }

    @Test
    void duplicateDeliveryForSameEventTemplateAndRecipientIsIgnored() {
        UUID sourceEventId = UUID.randomUUID();

        boolean firstEnqueue = emailDeliveryService.enqueueDelivery(
                sourceEventId,
                "CUSTOMER@example.com",
                EmailTemplateKey.TICKET_CREATED,
                templateModel());
        boolean duplicateEnqueue = emailDeliveryService.enqueueDelivery(
                sourceEventId,
                "customer@example.com",
                EmailTemplateKey.TICKET_CREATED,
                templateModel());

        assertThat(firstEnqueue).isTrue();
        assertThat(duplicateEnqueue).isFalse();
        assertThat(emailDeliveryRepository.findAll()).hasSize(1);
        assertThat(emailDeliveryRepository.findAll().getFirst().getRecipientEmail()).isEqualTo("customer@example.com");
    }

    @Test
    void sentDeliveryIsNotSentAgainWhenRetryLoopRunsRepeatedly() {
        UUID sourceEventId = UUID.randomUUID();
        emailDeliveryService.enqueueDelivery(
                sourceEventId,
                "customer@example.com",
                EmailTemplateKey.TICKET_CREATED,
                templateModel());
        assertThat(emailDeliveryRepository.findAll().getFirst().getStatus()).isEqualTo(EmailDeliveryStatus.PENDING);

        int firstRunCount = emailDeliveryService.processDueDeliveries();
        int secondRunCount = emailDeliveryService.processDueDeliveries();

        EmailDeliveryEntity delivery = emailDeliveryRepository.findAll().getFirst();
        assertThat(firstRunCount).isEqualTo(1);
        assertThat(secondRunCount).isZero();
        assertThat(recordingEmailSender.messages()).hasSize(1);
        assertThat(recordingEmailSender.statusesObservedDuringSend()).contains(EmailDeliveryStatus.RETRYING);
        assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
        assertThat(delivery.getSentAt()).isNotNull();
        assertThat(delivery.getNextAttemptAt()).isNull();
    }

    @Test
    void failedDeliveryIsRetriedAndThenMarkedSent() {
        UUID sourceEventId = UUID.randomUUID();
        recordingEmailSender.failNext(1);
        emailDeliveryService.enqueueDelivery(
                sourceEventId,
                "customer@example.com",
                EmailTemplateKey.TICKET_CREATED,
                templateModel());

        int failedRunCount = emailDeliveryService.processDueDeliveries();
        EmailDeliveryEntity failedDelivery = emailDeliveryRepository.findAll().getFirst();
        int retryRunCount = emailDeliveryService.processDueDeliveries();
        EmailDeliveryEntity sentDelivery = emailDeliveryRepository.findAll().getFirst();

        assertThat(failedRunCount).isEqualTo(1);
        assertThat(failedDelivery.getStatus()).isEqualTo(EmailDeliveryStatus.FAILED);
        assertThat(failedDelivery.getRetryCount()).isEqualTo(1);
        assertThat(failedDelivery.getNextAttemptAt()).isNotNull();
        assertThat(retryRunCount).isEqualTo(1);
        assertThat(recordingEmailSender.messages()).hasSize(1);
        assertThat(sentDelivery.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
        assertThat(sentDelivery.getRetryCount()).isEqualTo(1);
    }

    private static Map<String, Object> templateModel() {
        return Map.of(
                "customerName", "Customer",
                "ticketNumber", "TCK-3101",
                "priority", "HIGH",
                "status", "NEW",
                "ticketUrl", "https://app.ticket.local/tickets/TCK-3101");
    }

    @TestConfiguration
    static class EmailSenderTestConfig {

        @Bean
        @Primary
        RecordingEmailSenderPort recordingEmailSenderPort(EmailDeliveryJpaRepository emailDeliveryRepository) {
            return new RecordingEmailSenderPort(emailDeliveryRepository);
        }
    }

    static class RecordingEmailSenderPort implements EmailSenderPort {

        private final EmailDeliveryJpaRepository emailDeliveryRepository;
        private final List<EmailMessage> messages = new CopyOnWriteArrayList<>();
        private final List<EmailDeliveryStatus> statusesObservedDuringSend = new CopyOnWriteArrayList<>();
        private final AtomicInteger failuresBeforeSuccess = new AtomicInteger();

        RecordingEmailSenderPort(EmailDeliveryJpaRepository emailDeliveryRepository) {
            this.emailDeliveryRepository = emailDeliveryRepository;
        }

        @Override
        public void send(EmailMessage message) {
            statusesObservedDuringSend.add(emailDeliveryRepository.findAll().getFirst().getStatus());
            if (failuresBeforeSuccess.getAndUpdate(value -> Math.max(0, value - 1)) > 0) {
                throw new EmailDeliveryException("planned failure", new RuntimeException("smtp unavailable"));
            }
            messages.add(message);
        }

        void failNext(int failureCount) {
            failuresBeforeSuccess.set(failureCount);
        }

        List<EmailMessage> messages() {
            return List.copyOf(messages);
        }

        List<EmailDeliveryStatus> statusesObservedDuringSend() {
            return List.copyOf(statusesObservedDuringSend);
        }

        void reset() {
            messages.clear();
            statusesObservedDuringSend.clear();
            failuresBeforeSuccess.set(0);
        }
    }
}
