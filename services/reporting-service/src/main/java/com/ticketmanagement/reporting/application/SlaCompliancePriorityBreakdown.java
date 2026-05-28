package com.ticketmanagement.reporting.application;

import java.math.BigDecimal;
import java.util.Objects;

import com.ticketmanagement.reporting.domain.ProjectionPriority;

public record SlaCompliancePriorityBreakdown(
        ProjectionPriority priority,
        long metTicketCount,
        long breachedTicketCount,
        long atRiskTicketCount,
        long activeTicketCount,
        BigDecimal compliancePercentage) {

    public SlaCompliancePriorityBreakdown {
        priority = Objects.requireNonNull(priority, "priority must not be null");
        compliancePercentage = Objects.requireNonNull(compliancePercentage, "compliancePercentage must not be null");
        if (metTicketCount < 0 || breachedTicketCount < 0 || atRiskTicketCount < 0 || activeTicketCount < 0) {
            throw new IllegalArgumentException("SLA compliance counts must not be negative");
        }
    }
}
