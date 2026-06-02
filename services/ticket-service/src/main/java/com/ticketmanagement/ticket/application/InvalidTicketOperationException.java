package com.ticketmanagement.ticket.application;

import java.util.UUID;

import com.ticketmanagement.ticket.domain.TicketStatus;

public class InvalidTicketOperationException extends RuntimeException {

    private InvalidTicketOperationException(String message) {
        super(message);
    }

    public static InvalidTicketOperationException statusMustChange() {
        return new InvalidTicketOperationException("Ticket status must change");
    }

    public static InvalidTicketOperationException invalidStatusTransition(
            UUID ticketId,
            TicketStatus currentStatus,
            TicketStatus requestedStatus) {
        return new InvalidTicketOperationException(
                "Invalid ticket status transition for ticket %s: %s -> %s"
                        .formatted(ticketId, currentStatus, requestedStatus));
    }

    public static InvalidTicketOperationException invalidAssignmentTarget(UUID assigneeId, UUID teamId) {
        return new InvalidTicketOperationException(
                "Invalid assignment target: assignee %s is not an active member of team %s"
                        .formatted(assigneeId, teamId));
    }
}
