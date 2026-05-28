package com.ticketmanagement.event.workflow;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record SlaBreachedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID recipientId,
        String priority,
        Instant targetResolutionAt,
        Instant detectedAt,
        String breachReason) implements EventPayload {

    public SlaBreachedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        recipientId = Objects.requireNonNull(recipientId, "recipientId must not be null");
        priority = requireText(priority, "priority");
        targetResolutionAt = Objects.requireNonNull(targetResolutionAt, "targetResolutionAt must not be null");
        detectedAt = Objects.requireNonNull(detectedAt, "detectedAt must not be null");
        breachReason = requireText(breachReason, "breachReason");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
