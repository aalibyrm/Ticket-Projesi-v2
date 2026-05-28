package com.ticketmanagement.reporting.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record SlaComplianceReport(
        long metTicketCount,
        long breachedTicketCount,
        long atRiskTicketCount,
        long activeTicketCount,
        BigDecimal compliancePercentage,
        List<SlaCompliancePriorityBreakdown> priorityBreakdown,
        OffsetDateTime generatedAt) {

    public SlaComplianceReport {
        compliancePercentage = Objects.requireNonNull(compliancePercentage, "compliancePercentage must not be null");
        priorityBreakdown = List.copyOf(Objects.requireNonNull(priorityBreakdown, "priorityBreakdown must not be null"));
        generatedAt = Objects.requireNonNull(generatedAt, "generatedAt must not be null");
        if (metTicketCount < 0 || breachedTicketCount < 0 || atRiskTicketCount < 0 || activeTicketCount < 0) {
            throw new IllegalArgumentException("SLA compliance counts must not be negative");
        }
    }
}
