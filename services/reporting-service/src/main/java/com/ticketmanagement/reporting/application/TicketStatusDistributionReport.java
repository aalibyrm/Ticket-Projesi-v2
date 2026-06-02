package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record TicketStatusDistributionReport(
        List<TicketStatusCount> counts,
        List<DepartmentTicketCount> departmentCounts,
        List<TeamTicketCount> teamCounts,
        long totalOpenTickets,
        OffsetDateTime generatedAt) {

    public TicketStatusDistributionReport(
            List<TicketStatusCount> counts,
            long totalOpenTickets,
            OffsetDateTime generatedAt) {
        this(counts, List.of(), List.of(), totalOpenTickets, generatedAt);
    }

    public TicketStatusDistributionReport {
        counts = List.copyOf(Objects.requireNonNull(counts, "counts must not be null"));
        departmentCounts = List.copyOf(Objects.requireNonNull(departmentCounts, "departmentCounts must not be null"));
        teamCounts = List.copyOf(Objects.requireNonNull(teamCounts, "teamCounts must not be null"));
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt must not be null");
        if (totalOpenTickets < 0) {
            throw new IllegalArgumentException("totalOpenTickets must not be negative");
        }
    }
}
