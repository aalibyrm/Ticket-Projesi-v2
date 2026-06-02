package com.ticketmanagement.reporting.application;

import java.util.Objects;
import java.util.UUID;

public record TeamTicketCount(
        UUID assignedTeamId,
        Long count) {

    public TeamTicketCount {
        assignedTeamId = Objects.requireNonNull(assignedTeamId, "assignedTeamId must not be null");
        count = Objects.requireNonNull(count, "count must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }
}
