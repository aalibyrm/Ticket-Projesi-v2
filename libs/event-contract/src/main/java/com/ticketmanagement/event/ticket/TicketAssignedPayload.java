package com.ticketmanagement.event.ticket;

import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record TicketAssignedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID assigneeId,
        UUID assignedTeamId) implements EventPayload {

    public TicketAssignedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        if (assigneeId == null && assignedTeamId == null) {
            throw new IllegalArgumentException("assigneeId or assignedTeamId must be provided");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
