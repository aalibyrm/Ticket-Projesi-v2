package com.ticketmanagement.reporting.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AgentPerformanceRowResponse(
        UUID agentId,
        String agentDisplayName,
        long assignedTicketCount,
        long resolvedTicketCount,
        long totalWorklogMinutes,
        BigDecimal averageResolutionMinutes) {
}
