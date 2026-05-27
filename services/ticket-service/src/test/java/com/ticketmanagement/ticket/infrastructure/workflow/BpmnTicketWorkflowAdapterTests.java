package com.ticketmanagement.ticket.infrastructure.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ticketmanagement.ticket.application.InvalidTicketOperationException;
import com.ticketmanagement.ticket.application.TicketStatusTransition;
import com.ticketmanagement.ticket.domain.TicketStatus;

class BpmnTicketWorkflowAdapterTests {

    private final BpmnTicketWorkflowAdapter adapter = new BpmnTicketWorkflowAdapter();

    @Test
    void authorizesBpmnLifecycleTransitionsWithWorkflowSignal() {
        UUID ticketId = UUID.randomUUID();

        TicketStatusTransition transition = adapter.authorizeStatusTransition(
                ticketId,
                TicketStatus.IN_PROGRESS,
                TicketStatus.WAITING_FOR_CUSTOMER);

        assertThat(transition.previousStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(transition.newStatus()).isEqualTo(TicketStatus.WAITING_FOR_CUSTOMER);
        assertThat(transition.workflowSignal()).isEqualTo("REQUEST_CUSTOMER_INFO");
    }

    @Test
    void rejectsClosedTerminalStatusTransition() {
        UUID ticketId = UUID.randomUUID();

        assertThatThrownBy(() -> adapter.authorizeStatusTransition(
                ticketId,
                TicketStatus.CLOSED,
                TicketStatus.IN_PROGRESS))
                .isInstanceOf(InvalidTicketOperationException.class)
                .hasMessageContaining("CLOSED -> IN_PROGRESS");
    }
}
