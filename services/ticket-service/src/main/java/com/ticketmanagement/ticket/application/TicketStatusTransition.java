package com.ticketmanagement.ticket.application;

import com.ticketmanagement.ticket.domain.TicketStatus;

public record TicketStatusTransition(
        TicketStatus previousStatus,
        TicketStatus newStatus,
        String workflowSignal) {
}
