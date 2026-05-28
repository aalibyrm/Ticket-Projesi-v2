package com.ticketmanagement.reporting.application;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record AgentWorklogProjectionCommand(
        UUID worklogId,
        UUID ticketId,
        String ticketNumber,
        UUID agentId,
        LocalDate workDate,
        int durationMinutes) {

    public AgentWorklogProjectionCommand {
        worklogId = Objects.requireNonNull(worklogId, "worklogId must not be null");
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
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
        return value.trim();
    }
}
