package com.ticketmanagement.event.ticket;

import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record TicketStatusChangedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID customerId,
        String previousStatus,
        String newStatus) implements EventPayload {

    public TicketStatusChangedPayload(
            UUID ticketId,
            String ticketNumber,
            String previousStatus,
            String newStatus) {
        this(ticketId, ticketNumber, null, previousStatus, newStatus);
    }

    public TicketStatusChangedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        previousStatus = requireText(previousStatus, "previousStatus");
        newStatus = requireText(newStatus, "newStatus");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
