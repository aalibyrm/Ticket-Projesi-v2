package com.ticketmanagement.reporting.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import com.ticketmanagement.reporting.application.SlaComplianceReport;

public record SlaComplianceReportResponse(
        long metTicketCount,
        long breachedTicketCount,
        long atRiskTicketCount,
        long activeTicketCount,
        BigDecimal compliancePercentage,
        List<SlaCompliancePriorityResponse> priorityBreakdown,
        OffsetDateTime generatedAt) {

    public static SlaComplianceReportResponse from(SlaComplianceReport report) {
        return new SlaComplianceReportResponse(
                report.metTicketCount(),
                report.breachedTicketCount(),
                report.atRiskTicketCount(),
                report.activeTicketCount(),
                report.compliancePercentage(),
                report.priorityBreakdown()
                        .stream()
                        .map(priority -> new SlaCompliancePriorityResponse(
                                priority.priority().name(),
                                priority.metTicketCount(),
                                priority.breachedTicketCount(),
                                priority.atRiskTicketCount(),
                                priority.activeTicketCount(),
                                priority.compliancePercentage()))
                        .toList(),
                report.generatedAt());
    }
}
