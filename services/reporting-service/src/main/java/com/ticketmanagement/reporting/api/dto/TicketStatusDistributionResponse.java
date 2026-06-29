package com.ticketmanagement.reporting.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.ticketmanagement.reporting.application.TicketStatusDistributionReport;

public record TicketStatusDistributionResponse(
        List<TicketStatusCountResponse> counts,
        List<DepartmentTicketCountResponse> departmentCounts,
        List<TeamTicketCountResponse> teamCounts,
        long totalOpenTickets,
        OffsetDateTime generatedAt) {

    public static TicketStatusDistributionResponse from(TicketStatusDistributionReport report) {
        return new TicketStatusDistributionResponse(
                report.counts()
                        .stream()
                        .map(count -> new TicketStatusCountResponse(count.status().name(), count.count()))
                        .toList(),
                report.departmentCounts()
                        .stream()
                        .map(count -> new DepartmentTicketCountResponse(
                                count.routedDepartmentId(),
                                count.routedDepartmentCode(),
                                count.routedDepartmentName(),
                                count.count()))
                        .toList(),
                report.teamCounts()
                        .stream()
                        .map(count -> new TeamTicketCountResponse(
                                count.assignedTeamId(),
                                ReportingDisplayDirectory.teamCode(count.assignedTeamId()),
                                ReportingDisplayDirectory.teamName(count.assignedTeamId()),
                                count.count()))
                        .toList(),
                report.totalOpenTickets(),
                report.generatedAt());
    }
}
