package com.ticketmanagement.ticket.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record AgentSummaryMetrics(
        UUID agentId,
        long resolvedTicketCount,
        long slaMetTicketCount,
        long slaBreachedTicketCount,
        BigDecimal slaCompliancePercentage,
        boolean metricsAvailable) {

    public AgentSummaryMetrics(
            UUID agentId,
            long resolvedTicketCount,
            long slaMetTicketCount,
            long slaBreachedTicketCount,
            BigDecimal slaCompliancePercentage) {
        this(agentId, resolvedTicketCount, slaMetTicketCount, slaBreachedTicketCount, slaCompliancePercentage, true);
    }

    public static AgentSummaryMetrics empty(UUID agentId) {
        return new AgentSummaryMetrics(
                agentId,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                true);
    }

    public static AgentSummaryMetrics unavailable(UUID agentId) {
        return new AgentSummaryMetrics(
                agentId,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                false);
    }
}
