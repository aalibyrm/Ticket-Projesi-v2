package com.ticketmanagement.reporting.domain;

import java.util.Locale;

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
}
