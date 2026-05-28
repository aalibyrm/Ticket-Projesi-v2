package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

public record TicketProjectionUpsertCommand(
        UUID ticketId,
        String ticketNumber,
        UUID customerId,
        UUID productId,
        ProjectionPriority priority,
        ProjectionTicketStatus status,
        UUID assigneeId,
        UUID assignedTeamId,
        OffsetDateTime openedAt,
        OffsetDateTime updatedAt,
        OffsetDateTime closedAt,
        OffsetDateTime slaTargetResolutionAt,
        ProjectionSlaStatus slaStatus) {

    public TicketProjectionUpsertCommand {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        productId = Objects.requireNonNull(productId, "productId must not be null");
        priority = Objects.requireNonNull(priority, "priority must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        openedAt = Objects.requireNonNull(openedAt, "openedAt must not be null");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        if (updatedAt.isBefore(openedAt)) {
            throw new IllegalArgumentException("updatedAt must not be before openedAt");
        }
        if (closedAt != null && closedAt.isBefore(openedAt)) {
            throw new IllegalArgumentException("closedAt must not be before openedAt");
        }
        if (status == ProjectionTicketStatus.CLOSED && closedAt == null) {
            throw new IllegalArgumentException("closedAt must be provided for closed tickets");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
