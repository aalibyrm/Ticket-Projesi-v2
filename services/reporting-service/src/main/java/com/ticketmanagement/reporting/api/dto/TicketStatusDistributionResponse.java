package com.ticketmanagement.reporting.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.ticketmanagement.reporting.application.TicketStatusDistributionReport;

public record TicketStatusDistributionResponse(
        List<TicketStatusCountResponse> counts,
        long totalOpenTickets,
        OffsetDateTime generatedAt) {

    public static TicketStatusDistributionResponse from(TicketStatusDistributionReport report) {
        return new TicketStatusDistributionResponse(
                report.counts()
                        .stream()
                        .map(count -> new TicketStatusCountResponse(count.status().name(), count.count()))
                        .toList(),
                report.totalOpenTickets(),
                report.generatedAt());
    }
}
