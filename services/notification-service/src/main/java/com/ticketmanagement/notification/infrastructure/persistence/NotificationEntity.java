package com.ticketmanagement.notification.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.ticketmanagement.notification.domain.NotificationType;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "notifications", schema = "notification_schema")
public class NotificationEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false, unique = true)
    private UUID sourceEventId;

    @Column(nullable = false, updatable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private NotificationType type;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "read_flag", nullable = false)
    private boolean read;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static NotificationEntity ticketCreated(UUID id, UUID sourceEventId, UUID recipientId, String ticketNumber) {
        NotificationEntity notification = new NotificationEntity();
        notification.id = id;
        notification.sourceEventId = sourceEventId;
        notification.recipientId = recipientId;
        notification.type = NotificationType.TICKET_CREATED;
        notification.title = "Ticket created";
        notification.message = "Ticket " + ticketNumber + " was created.";
        notification.read = false;
        return notification;
    }

    public static NotificationEntity externalCommentAdded(
            UUID id,
            UUID sourceEventId,
            UUID recipientId,
            String ticketNumber) {
        NotificationEntity notification = new NotificationEntity();
        notification.id = id;
        notification.sourceEventId = sourceEventId;
        notification.recipientId = recipientId;
        notification.type = NotificationType.TICKET_EXTERNAL_COMMENT_ADDED;
        notification.title = "New ticket message";
        notification.message = "Ticket " + ticketNumber + " has a new message.";
        notification.read = false;
        return notification;
    }

    public static NotificationEntity slaRisk(UUID id, UUID sourceEventId, UUID recipientId, String ticketNumber) {
        NotificationEntity notification = new NotificationEntity();
        notification.id = id;
        notification.sourceEventId = sourceEventId;
        notification.recipientId = recipientId;
        notification.type = NotificationType.SLA_RISK;
        notification.title = "SLA risk detected";
        notification.message = "Ticket " + ticketNumber + " is approaching its SLA deadline.";
        notification.read = false;
        return notification;
    }

    public static NotificationEntity slaBreach(UUID id, UUID sourceEventId, UUID recipientId, String ticketNumber) {
        NotificationEntity notification = new NotificationEntity();
        notification.id = id;
        notification.sourceEventId = sourceEventId;
        notification.recipientId = recipientId;
        notification.type = NotificationType.SLA_BREACH;
        notification.title = "SLA breached";
        notification.message = "Ticket " + ticketNumber + " missed its SLA deadline.";
        notification.read = false;
        return notification;
    }

    public void markRead() {
        this.read = true;
    }

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
