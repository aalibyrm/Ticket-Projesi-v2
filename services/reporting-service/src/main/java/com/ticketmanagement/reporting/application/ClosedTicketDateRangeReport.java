package com.ticketmanagement.reporting.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record ClosedTicketDateRangeReport(
        LocalDate fromDate,
        LocalDate toDate,
        long totalClosedTickets,
        BigDecimal averageResolutionMinutes,
        List<ClosedTicketDailyCount> dailyCounts,
        List<ClosedTicketPriorityCount> priorityCounts,
        OffsetDateTime generatedAt) {

    public ClosedTicketDateRangeReport {
        fromDate = Objects.requireNonNull(fromDate, "fromDate must not be null");
        toDate = Objects.requireNonNull(toDate, "toDate must not be null");
        averageResolutionMinutes = Objects.requireNonNull(
                averageResolutionMinutes,
                "averageResolutionMinutes must not be null");
        dailyCounts = List.copyOf(Objects.requireNonNull(dailyCounts, "dailyCounts must not be null"));
        priorityCounts = List.copyOf(Objects.requireNonNull(priorityCounts, "priorityCounts must not be null"));
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt must not be null");
        if (totalClosedTickets < 0) {
            throw new IllegalArgumentException("totalClosedTickets must not be negative");
        }
    }
}
