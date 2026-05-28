package com.ticketmanagement.reporting.domain;

import java.util.Locale;

public enum ProjectionPriority {
    LOW,
    MEDIUM,
    HIGH;

    public static ProjectionPriority from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("priority must not be blank");
        }
        return ProjectionPriority.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
