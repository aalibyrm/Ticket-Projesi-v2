package com.ticketmanagement.reporting.application;

import java.math.BigDecimal;
import java.util.UUID;

public record AgentSummary(
        UUID agentId,
        long resolvedTicketCount,
        long slaMetTicketCount,
        long slaBreachedTicketCount,
        BigDecimal slaCompliancePercentage) {
}
