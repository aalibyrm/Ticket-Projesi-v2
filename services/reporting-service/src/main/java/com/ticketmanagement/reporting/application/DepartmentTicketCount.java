package com.ticketmanagement.reporting.application;

import java.util.Objects;
import java.util.UUID;

public record DepartmentTicketCount(
        UUID routedDepartmentId,
        String routedDepartmentCode,
        String routedDepartmentName,
        Long count) {

    public DepartmentTicketCount {
        routedDepartmentId = Objects.requireNonNull(routedDepartmentId, "routedDepartmentId must not be null");
        count = Objects.requireNonNull(count, "count must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }
}
