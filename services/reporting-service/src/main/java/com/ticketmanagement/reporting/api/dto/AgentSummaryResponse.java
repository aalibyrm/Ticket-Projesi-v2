package com.ticketmanagement.reporting.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.ticketmanagement.reporting.application.AgentSummary;

public record AgentSummaryResponse(
        UUID agentId,
        long resolvedTicketCount,
        long slaMetTicketCount,
        long slaBreachedTicketCount,
        BigDecimal slaCompliancePercentage) {

    public static AgentSummaryResponse from(AgentSummary summary) {
        return new AgentSummaryResponse(
                summary.agentId(),
                summary.resolvedTicketCount(),
                summary.slaMetTicketCount(),
                summary.slaBreachedTicketCount(),
                summary.slaCompliancePercentage());
    }
}
