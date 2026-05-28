package com.ticketmanagement.workflow.application;

import java.util.Objects;
import java.util.UUID;

public record TicketAssignmentCommand(
        UUID ticketId,
        UUID assigneeId,
        UUID assignedTeamId) {

    public TicketAssignmentCommand {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        assigneeId = Objects.requireNonNull(assigneeId, "assigneeId must not be null");
    }
}
