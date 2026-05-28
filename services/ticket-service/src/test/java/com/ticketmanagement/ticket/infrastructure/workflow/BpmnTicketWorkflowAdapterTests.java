package com.ticketmanagement.ticket.infrastructure.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.ticketmanagement.ticket.application.InvalidTicketOperationException;
import com.ticketmanagement.ticket.application.TicketStatusTransition;
import com.ticketmanagement.ticket.domain.TicketStatus;

class BpmnTicketWorkflowAdapterTests {

    private final BpmnTicketWorkflowAdapter adapter = new BpmnTicketWorkflowAdapter();

    @Test
    void authorizesEveryBpmnLifecycleTransitionWithWorkflowSignal() {
        UUID ticketId = UUID.randomUUID();

        List<ExpectedTransition> expectedTransitions = List.of(
                new ExpectedTransition(TicketStatus.NEW, TicketStatus.IN_PROGRESS, "START_PROGRESS"),
                new ExpectedTransition(TicketStatus.IN_PROGRESS, TicketStatus.WAITING_FOR_CUSTOMER, "REQUEST_CUSTOMER_INFO"),
                new ExpectedTransition(TicketStatus.WAITING_FOR_CUSTOMER, TicketStatus.IN_PROGRESS, "CUSTOMER_RESPONDED"),
                new ExpectedTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, "RESOLVE_TICKET"),
                new ExpectedTransition(TicketStatus.RESOLVED, TicketStatus.CLOSED, "CLOSE_TICKET"),
                new ExpectedTransition(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS, "REOPEN_TICKET"));

        expectedTransitions.forEach(expected -> {
            TicketStatusTransition transition = adapter.authorizeStatusTransition(
                    ticketId,
                    expected.currentStatus(),
                    expected.requestedStatus());

            assertThat(transition.previousStatus()).isEqualTo(expected.currentStatus());
            assertThat(transition.newStatus()).isEqualTo(expected.requestedStatus());
            assertThat(transition.workflowSignal()).isEqualTo(expected.workflowSignal());
        });
    }

    @Test
    void rejectsInvalidLifecycleTransitionsWithoutWorkflowSignal() {
        UUID ticketId = UUID.randomUUID();

        List<ExpectedRejectedTransition> rejectedTransitions = List.of(
                new ExpectedRejectedTransition(TicketStatus.NEW, TicketStatus.CLOSED),
                new ExpectedRejectedTransition(TicketStatus.NEW, TicketStatus.RESOLVED),
                new ExpectedRejectedTransition(TicketStatus.WAITING_FOR_CUSTOMER, TicketStatus.RESOLVED),
                new ExpectedRejectedTransition(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS));

        rejectedTransitions.forEach(rejected -> assertThatThrownBy(() -> adapter.authorizeStatusTransition(
                ticketId,
                rejected.currentStatus(),
                rejected.requestedStatus()))
                .isInstanceOf(InvalidTicketOperationException.class)
                .hasMessageContaining(rejected.currentStatus() + " -> " + rejected.requestedStatus()));
    }

    @Test
    void rejectsNoopStatusTransition() {
        UUID ticketId = UUID.randomUUID();

        assertThatThrownBy(() -> adapter.authorizeStatusTransition(
                ticketId,
                TicketStatus.IN_PROGRESS,
                TicketStatus.IN_PROGRESS))
                .isInstanceOf(InvalidTicketOperationException.class)
                .hasMessageContaining("must change");
    }

    private record ExpectedTransition(
            TicketStatus currentStatus,
            TicketStatus requestedStatus,
            String workflowSignal) {
    }

    private record ExpectedRejectedTransition(
            TicketStatus currentStatus,
            TicketStatus requestedStatus) {
    }
}
