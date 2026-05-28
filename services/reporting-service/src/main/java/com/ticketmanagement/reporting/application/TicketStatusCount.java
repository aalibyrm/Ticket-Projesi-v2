package com.ticketmanagement.reporting.application;

import java.util.Objects;

import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

public record TicketStatusCount(
        ProjectionTicketStatus status,
        Long count) {

    public TicketStatusCount {
        status = Objects.requireNonNull(status, "status must not be null");
        count = Objects.requireNonNull(count, "count must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }
}
