package com.ticketmanagement.reporting.domain;

import java.util.Locale;

public enum ProjectionSlaStatus {
    ACTIVE,
    AT_RISK,
    BREACHED,
    MET;

    public static ProjectionSlaStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("slaStatus must not be blank");
        }
        return ProjectionSlaStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
