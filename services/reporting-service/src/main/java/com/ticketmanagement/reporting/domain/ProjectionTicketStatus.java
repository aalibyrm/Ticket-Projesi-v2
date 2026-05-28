package com.ticketmanagement.reporting.domain;

import java.util.Locale;
import java.util.List;

public enum ProjectionTicketStatus {
    NEW,
    IN_PROGRESS,
    WAITING_FOR_CUSTOMER,
    RESOLVED,
    CLOSED;

    public static ProjectionTicketStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
        return ProjectionTicketStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public static List<ProjectionTicketStatus> openStatuses() {
        return List.of(NEW, IN_PROGRESS, WAITING_FOR_CUSTOMER, RESOLVED);
    }
}
