package com.ticketmanagement.workflow.domain;

public enum SlaPriority {
    LOW,
    MEDIUM,
    HIGH;

    public static SlaPriority from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SLA priority must not be blank");
        }
        return SlaPriority.valueOf(value.trim().toUpperCase());
    }
}
