package com.ticketmanagement.reporting.api.dto;

import java.math.BigDecimal;

public record SlaCompliancePriorityResponse(
        String priority,
        long metTicketCount,
        long breachedTicketCount,
        long atRiskTicketCount,
        long activeTicketCount,
        BigDecimal compliancePercentage) {
}
