package com.ticketmanagement.reporting.application;

import java.util.Objects;

import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;

public record SlaComplianceStatusCount(
        ProjectionPriority priority,
        ProjectionSlaStatus status,
        long count) {

    public SlaComplianceStatusCount {
        priority = Objects.requireNonNull(priority, "priority must not be null");
        status = Objects.requireNonNull(status, "status must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }
}
