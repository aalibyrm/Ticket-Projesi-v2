package com.ticketmanagement.event.ticket;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record WorklogAddedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID worklogId,
        UUID agentId,
        LocalDate workDate,
        int durationMinutes) implements EventPayload {

    public WorklogAddedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        worklogId = Objects.requireNonNull(worklogId, "worklogId must not be null");
        agentId = Objects.requireNonNull(agentId, "agentId must not be null");
        workDate = Objects.requireNonNull(workDate, "workDate must not be null");
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("durationMinutes must be positive");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
