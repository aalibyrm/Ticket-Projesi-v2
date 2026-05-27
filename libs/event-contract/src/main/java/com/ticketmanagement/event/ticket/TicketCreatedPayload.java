package com.ticketmanagement.event.ticket;

import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record TicketCreatedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID customerId,
        UUID productId,
        String priority,
        String status) implements EventPayload {

    public TicketCreatedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        productId = Objects.requireNonNull(productId, "productId must not be null");
        priority = requireText(priority, "priority");
        status = requireText(status, "status");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
