package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record TicketStatusDistributionReport(
        List<TicketStatusCount> counts,
        long totalOpenTickets,
        OffsetDateTime generatedAt) {

    public TicketStatusDistributionReport {
        counts = List.copyOf(Objects.requireNonNull(counts, "counts must not be null"));
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt must not be null");
        if (totalOpenTickets < 0) {
            throw new IllegalArgumentException("totalOpenTickets must not be negative");
        }
    }
}
