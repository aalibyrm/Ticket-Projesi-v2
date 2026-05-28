package com.ticketmanagement.workflow.application;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.workflow.domain.SlaPriority;

public record TicketSlaCreationCommand(
        UUID ticketId,
        String ticketNumber,
        UUID customerId,
        SlaPriority priority,
        OffsetDateTime openedAt) {

    public TicketSlaCreationCommand {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        if (ticketNumber == null || ticketNumber.isBlank()) {
            throw new IllegalArgumentException("ticketNumber must not be blank");
        }
        ticketNumber = ticketNumber.trim();
        customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        priority = Objects.requireNonNull(priority, "priority must not be null");
        openedAt = Objects.requireNonNull(openedAt, "openedAt must not be null");
    }
}
