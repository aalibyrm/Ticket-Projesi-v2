package com.ticketmanagement.reporting.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.ticketmanagement.reporting.application.AgentPerformanceReport;

public record AgentPerformanceReportResponse(
        List<AgentPerformanceRowResponse> rows,
        OffsetDateTime generatedAt) {

    public static AgentPerformanceReportResponse from(AgentPerformanceReport report) {
        return new AgentPerformanceReportResponse(
                report.rows()
                        .stream()
                        .map(row -> new AgentPerformanceRowResponse(
                                row.agentId(),
                                row.assignedTicketCount(),
                                row.resolvedTicketCount(),
                                row.totalWorklogMinutes(),
                                row.averageResolutionMinutes()))
                        .toList(),
                report.generatedAt());
    }
}
