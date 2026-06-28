package com.ticketmanagement.ticket.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record AgentSummaryMetrics(
        UUID agentId,
        long resolvedTicketCount,
        long slaMetTicketCount,
        long slaBreachedTicketCount,
        BigDecimal slaCompliancePercentage) {

    public static AgentSummaryMetrics empty(UUID agentId) {
        return new AgentSummaryMetrics(
                agentId,
                0,
                0,
                0,
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }
}
