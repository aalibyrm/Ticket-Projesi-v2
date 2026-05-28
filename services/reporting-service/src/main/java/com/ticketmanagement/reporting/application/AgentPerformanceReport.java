package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record AgentPerformanceReport(
        List<AgentPerformanceRow> rows,
        OffsetDateTime generatedAt) {

    public AgentPerformanceReport {
        rows = List.copyOf(Objects.requireNonNull(rows, "rows must not be null"));
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt must not be null");
    }
}
