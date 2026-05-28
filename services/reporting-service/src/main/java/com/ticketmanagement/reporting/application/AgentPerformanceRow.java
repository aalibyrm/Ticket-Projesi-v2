package com.ticketmanagement.reporting.application;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record AgentPerformanceRow(
        UUID agentId,
        long assignedTicketCount,
        long resolvedTicketCount,
        long totalWorklogMinutes,
        BigDecimal averageResolutionMinutes) {

    public AgentPerformanceRow {
        agentId = Objects.requireNonNull(agentId, "agentId must not be null");
        averageResolutionMinutes = Objects.requireNonNull(
                averageResolutionMinutes,
                "averageResolutionMinutes must not be null");
        if (assignedTicketCount < 0 || resolvedTicketCount < 0 || totalWorklogMinutes < 0) {
            throw new IllegalArgumentException("agent performance counts must not be negative");
        }
    }
}
