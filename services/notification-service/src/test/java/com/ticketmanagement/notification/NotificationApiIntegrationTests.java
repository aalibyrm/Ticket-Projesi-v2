package com.ticketmanagement.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.notification.api.dto.NotificationResponse;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class NotificationApiIntegrationTests {

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
    private NotificationJpaRepository notificationRepository;

    @BeforeEach
    void cleanNotificationData() {
        jdbcTemplate.update("delete from notification_schema.email_deliveries");
        jdbcTemplate.update("delete from notification_schema.notifications");
        jdbcTemplate.update("delete from notification_schema.processed_events");
    }

    @Test
    void listsOnlyRequesterNotificationsWithReadFilter() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        NotificationEntity ownUnread = ticketCreatedNotification(userId, "TCK-4001");
        NotificationEntity ownRead = ticketCreatedNotification(userId, "TCK-4002");
        ownRead.markRead();
        NotificationEntity otherUnread = ticketCreatedNotification(otherUserId, "TCK-4003");
        notificationRepository.saveAllAndFlush(List.of(ownUnread, ownRead, otherUnread));

        ResponseEntity<List<NotificationResponse>> allResponse = exchangeList(
                "/api/notifications",
                actorHeaders(userId));
        ResponseEntity<List<NotificationResponse>> unreadResponse = exchangeList(
                "/api/notifications?read=false",
                actorHeaders(userId));
        ResponseEntity<List<NotificationResponse>> readResponse = exchangeList(
                "/api/notifications?read=true",
                actorHeaders(userId));

        assertThat(allResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allResponse.getBody())
                .extracting(NotificationResponse::id)
                .containsExactlyInAnyOrder(ownUnread.getId(), ownRead.getId())
                .doesNotContain(otherUnread.getId());
        assertThat(unreadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(unreadResponse.getBody())
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.id()).isEqualTo(ownUnread.getId());
                    assertThat(notification.read()).isFalse();
                });
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readResponse.getBody())
                .singleElement()
                .satisfies(notification -> {
                    assertThat(notification.id()).isEqualTo(ownRead.getId());
                    assertThat(notification.read()).isTrue();
                });
    }

    @Test
    void marksOnlyRequesterNotificationAsRead() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        NotificationEntity ownUnread = ticketCreatedNotification(userId, "TCK-5001");
        NotificationEntity otherUnread = ticketCreatedNotification(otherUserId, "TCK-5002");
        notificationRepository.saveAllAndFlush(List.of(ownUnread, otherUnread));

        ResponseEntity<NotificationResponse> ownResponse = restTemplate.exchange(
                "/api/notifications/{id}/read",
                HttpMethod.PATCH,
                new HttpEntity<>(actorHeaders(userId)),
                NotificationResponse.class,
                ownUnread.getId());
        ResponseEntity<String> otherResponse = restTemplate.exchange(
                "/api/notifications/{id}/read",
                HttpMethod.PATCH,
                new HttpEntity<>(actorHeaders(userId)),
                String.class,
                otherUnread.getId());

        assertThat(ownResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ownResponse.getBody()).isNotNull();
        assertThat(ownResponse.getBody().read()).isTrue();
        assertThat(notificationRepository.findById(ownUnread.getId()))
                .isPresent()
                .get()
                .extracting(NotificationEntity::isRead)
                .isEqualTo(true);
        assertThat(otherResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notificationRepository.findById(otherUnread.getId()))
                .isPresent()
                .get()
                .extracting(NotificationEntity::isRead)
                .isEqualTo(false);
    }

    private ResponseEntity<List<NotificationResponse>> exchangeList(String url, HttpHeaders headers) {
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                });
    }

    private static NotificationEntity ticketCreatedNotification(UUID recipientId, String ticketNumber) {
        return NotificationEntity.ticketCreated(
                UUID.randomUUID(),
                UUID.randomUUID(),
                recipientId,
                ticketNumber);
    }

    private static HttpHeaders actorHeaders(UUID actorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", actorId.toString());
        return headers;
    }
}
