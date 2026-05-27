package com.ticketmanagement.ticket.infrastructure.workflow;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.ticketmanagement.ticket.application.InvalidTicketOperationException;
import com.ticketmanagement.ticket.application.TicketStatusTransition;
import com.ticketmanagement.ticket.application.TicketWorkflowPort;
import com.ticketmanagement.ticket.domain.TicketStatus;

@Component
class BpmnTicketWorkflowAdapter implements TicketWorkflowPort {

    private static final Map<TicketStatus, Map<TicketStatus, String>> ALLOWED_TRANSITIONS = Map.of(
            TicketStatus.NEW, Map.of(
                    TicketStatus.IN_PROGRESS, "START_PROGRESS"),
            TicketStatus.IN_PROGRESS, Map.of(
                    TicketStatus.WAITING_FOR_CUSTOMER, "REQUEST_CUSTOMER_INFO",
                    TicketStatus.RESOLVED, "RESOLVE_TICKET"),
            TicketStatus.WAITING_FOR_CUSTOMER, Map.of(
                    TicketStatus.IN_PROGRESS, "CUSTOMER_RESPONDED"),
            TicketStatus.RESOLVED, Map.of(
                    TicketStatus.CLOSED, "CLOSE_TICKET",
                    TicketStatus.IN_PROGRESS, "REOPEN_TICKET"),
            TicketStatus.CLOSED, Map.of());

    @Override
    public TicketStatusTransition authorizeStatusTransition(
            UUID ticketId,
            TicketStatus currentStatus,
            TicketStatus requestedStatus) {
        if (currentStatus == requestedStatus) {
            throw InvalidTicketOperationException.statusMustChange();
        }

        String workflowSignal = ALLOWED_TRANSITIONS
                .getOrDefault(currentStatus, Map.of())
                .get(requestedStatus);
        if (workflowSignal == null) {
            throw InvalidTicketOperationException.invalidStatusTransition(ticketId, currentStatus, requestedStatus);
        }

        return new TicketStatusTransition(currentStatus, requestedStatus, workflowSignal);
    }
}
