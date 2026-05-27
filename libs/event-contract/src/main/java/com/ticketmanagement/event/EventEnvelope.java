package com.ticketmanagement.event;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EventEnvelope<T extends EventPayload>(
        UUID eventId,
        String eventType,
        int version,
        Instant occurredAt,
        UUID actorId,
        String aggregateType,
        UUID aggregateId,
        String correlationId,
        T payload) {

    public EventEnvelope {
        eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        eventType = requireText(eventType, "eventType");
        version = EventVersionPolicy.requireSupported(version);
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        actorId = Objects.requireNonNull(actorId, "actorId must not be null");
        aggregateType = requireText(aggregateType, "aggregateType");
        aggregateId = Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        correlationId = normalizeOptionalText(correlationId);
        payload = Objects.requireNonNull(payload, "payload must not be null");
    }

    public static <T extends EventPayload> EventEnvelope<T> of(
            EventType eventType,
            UUID actorId,
            UUID aggregateId,
            T payload) {
        return of(eventType, actorId, aggregateId, null, payload);
    }

    public static <T extends EventPayload> EventEnvelope<T> of(
            EventType eventType,
            UUID actorId,
            UUID aggregateId,
            String correlationId,
            T payload) {
        return of(eventType, actorId, aggregateId, correlationId, payload, Clock.systemUTC());
    }

    static <T extends EventPayload> EventEnvelope<T> of(
            EventType eventType,
            UUID actorId,
            UUID aggregateId,
            String correlationId,
            T payload,
            Clock clock) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(clock, "clock must not be null");
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType.eventName(),
                eventType.version(),
                Instant.now(clock),
                actorId,
                eventType.aggregateType(),
                aggregateId,
                correlationId,
                payload);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
