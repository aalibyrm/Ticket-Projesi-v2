package com.ticketmanagement.reporting.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.ticketmanagement.reporting.application.ClosedTicketDateRangeReport;

public record ClosedTicketDateRangeResponse(
        LocalDate fromDate,
        LocalDate toDate,
        long totalClosedTickets,
        BigDecimal averageResolutionMinutes,
        List<ClosedTicketDailyCountResponse> dailyCounts,
        List<ClosedTicketPriorityCountResponse> priorityCounts,
        OffsetDateTime generatedAt) {

    public static ClosedTicketDateRangeResponse from(ClosedTicketDateRangeReport report) {
        return new ClosedTicketDateRangeResponse(
                report.fromDate(),
                report.toDate(),
                report.totalClosedTickets(),
                report.averageResolutionMinutes(),
                report.dailyCounts()
                        .stream()
                        .map(count -> new ClosedTicketDailyCountResponse(count.date(), count.count()))
                        .toList(),
                report.priorityCounts()
                        .stream()
                        .map(count -> new ClosedTicketPriorityCountResponse(count.priority().name(), count.count()))
                        .toList(),
                report.generatedAt());
    }
}
