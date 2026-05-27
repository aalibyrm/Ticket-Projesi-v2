package com.ticketmanagement.notification.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.ticketmanagement.notification.domain.EmailDeliveryStatus;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "email_deliveries", schema = "notification_schema")
public class EmailDeliveryEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID sourceEventId;

    @Column(nullable = false, length = 320)
    private String recipientEmail;

    @Column(nullable = false, length = 180)
    private String subject;

    @Column(nullable = false, length = 120)
    private String templateKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode templateModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailDeliveryStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 1000)
    private String lastError;

    private OffsetDateTime nextAttemptAt;

    private OffsetDateTime sentAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static EmailDeliveryEntity pending(
            UUID id,
            UUID sourceEventId,
            String recipientEmail,
            String subject,
            String templateKey,
            JsonNode templateModel) {
        EmailDeliveryEntity delivery = new EmailDeliveryEntity();
        delivery.id = Objects.requireNonNull(id, "id must not be null");
        delivery.sourceEventId = Objects.requireNonNull(sourceEventId, "sourceEventId must not be null");
        delivery.recipientEmail = requireText(recipientEmail, "recipientEmail").toLowerCase(Locale.ROOT);
        delivery.subject = requireText(subject, "subject");
        delivery.templateKey = requireText(templateKey, "templateKey");
        delivery.templateModel = Objects.requireNonNull(templateModel, "templateModel must not be null");
        delivery.status = EmailDeliveryStatus.PENDING;
        delivery.retryCount = 0;
        return delivery;
    }

    public void markRetrying(OffsetDateTime leaseExpiresAt) {
        this.status = EmailDeliveryStatus.RETRYING;
        this.nextAttemptAt = Objects.requireNonNull(leaseExpiresAt, "leaseExpiresAt must not be null");
    }

    public void markSent(OffsetDateTime sentAt) {
        this.status = EmailDeliveryStatus.SENT;
        this.sentAt = Objects.requireNonNull(sentAt, "sentAt must not be null");
        this.lastError = null;
        this.nextAttemptAt = null;
    }

    public void markFailed(String lastError, OffsetDateTime nextAttemptAt) {
        this.status = EmailDeliveryStatus.FAILED;
        this.retryCount++;
        this.lastError = truncate(lastError);
        this.nextAttemptAt = nextAttemptAt;
    }

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }
}
