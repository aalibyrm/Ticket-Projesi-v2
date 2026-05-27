package com.ticketmanagement.ticket.application;

import java.util.UUID;

import com.ticketmanagement.ticket.domain.TicketStatus;

public interface TicketWorkflowPort {

    TicketStatusTransition authorizeStatusTransition(
            UUID ticketId,
            TicketStatus currentStatus,
            TicketStatus requestedStatus);
}
