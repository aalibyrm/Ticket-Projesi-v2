package com.ticketmanagement.ticket.api.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import com.ticketmanagement.ticket.application.AgentSummaryMetrics;

public record TicketAgentSummaryResponse(
        boolean assigned,
        UUID agentId,
        String displayName,
        String email,
        UUID assignedTeamId,
        long resolvedTicketCount,
        long slaMetTicketCount,
        long slaBreachedTicketCount,
        BigDecimal slaCompliancePercentage,
        boolean metricsAvailable) {

    public static TicketAgentSummaryResponse unassigned(UUID assignedTeamId) {
        return new TicketAgentSummaryResponse(
                false,
                null,
                null,
                null,
                assignedTeamId,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                true);
    }

    public static TicketAgentSummaryResponse assigned(
            AgentSummaryMetrics metrics,
            String displayName,
            String email,
            UUID assignedTeamId) {
        return new TicketAgentSummaryResponse(
                true,
                metrics.agentId(),
                displayName,
                email,
                assignedTeamId,
                metrics.resolvedTicketCount(),
                metrics.slaMetTicketCount(),
                metrics.slaBreachedTicketCount(),
                metrics.slaCompliancePercentage(),
                metrics.metricsAvailable());
    }
}
