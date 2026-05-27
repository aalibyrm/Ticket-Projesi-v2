package com.ticketmanagement.ticket.infrastructure.outbox;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventTopic;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox_events", schema = "ticket_schema")
public class OutboxEventEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String topicName;

    @Column(nullable = false, length = 160)
    private String eventType;

    @Column(nullable = false)
    private int eventVersion;

    @Column(nullable = false, length = 80)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private UUID actorId;

    @Column(length = 160)
    private String correlationId;

    @Column(nullable = false)
    private OffsetDateTime occurredAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 1000)
    private String lastError;

    private OffsetDateTime nextAttemptAt;

    private OffsetDateTime publishedAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static OutboxEventEntity pending(EventTopic topic, EventEnvelope<?> envelope, JsonNode payload) {
        Objects.requireNonNull(topic, "topic must not be null");
        Objects.requireNonNull(envelope, "envelope must not be null");
        Objects.requireNonNull(payload, "payload must not be null");

        OutboxEventEntity event = new OutboxEventEntity();
        event.id = envelope.eventId();
        event.topicName = topic.topicName();
        event.eventType = envelope.eventType();
        event.eventVersion = envelope.version();
        event.aggregateType = envelope.aggregateType();
        event.aggregateId = envelope.aggregateId();
        event.actorId = envelope.actorId();
        event.correlationId = envelope.correlationId();
        event.occurredAt = OffsetDateTime.ofInstant(envelope.occurredAt(), ZoneOffset.UTC);
        event.payload = payload;
        event.status = OutboxEventStatus.PENDING;
        event.retryCount = 0;
        return event;
    }

    public void markProcessing(OffsetDateTime lockedUntil) {
        this.status = OutboxEventStatus.PROCESSING;
        this.nextAttemptAt = Objects.requireNonNull(lockedUntil, "lockedUntil must not be null");
    }

    public void markPublished(OffsetDateTime publishedAt) {
        this.status = OutboxEventStatus.PUBLISHED;
        this.publishedAt = Objects.requireNonNull(publishedAt, "publishedAt must not be null");
        this.lastError = null;
        this.nextAttemptAt = null;
    }

    public void markFailed(String lastError, OffsetDateTime nextAttemptAt) {
        this.status = OutboxEventStatus.FAILED;
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

    private static String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }
}
