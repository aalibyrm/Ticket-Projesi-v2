package com.ticketmanagement.notification.infrastructure.kafka;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

public record ConsumedEvent(
        UUID eventId,
        String eventType,
        int version,
        OffsetDateTime occurredAt,
        UUID actorId,
        String aggregateType,
        UUID aggregateId,
        String correlationId,
        JsonNode payload) {
}
