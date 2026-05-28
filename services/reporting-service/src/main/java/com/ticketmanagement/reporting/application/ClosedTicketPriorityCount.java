package com.ticketmanagement.reporting.application;

import java.util.Objects;

import com.ticketmanagement.reporting.domain.ProjectionPriority;

public record ClosedTicketPriorityCount(
        ProjectionPriority priority,
        long count) {

    public ClosedTicketPriorityCount {
        priority = Objects.requireNonNull(priority, "priority must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }
}
