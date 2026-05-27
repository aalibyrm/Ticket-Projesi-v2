package com.ticketmanagement.event.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TicketLifecyclePayloadTests {

    @Test
    void createsStatusChangedPayload() {
        UUID ticketId = UUID.randomUUID();

        TicketStatusChangedPayload payload = new TicketStatusChangedPayload(
                ticketId,
                "TCK-1001",
                "NEW",
                "IN_PROGRESS");

        assertEquals(ticketId, payload.ticketId());
        assertEquals("NEW", payload.previousStatus());
        assertEquals("IN_PROGRESS", payload.newStatus());
    }

    @Test
    void createsAssignedPayloadWithOptionalTeam() {
        UUID ticketId = UUID.randomUUID();
        UUID assigneeId = UUID.randomUUID();

        TicketAssignedPayload payload = new TicketAssignedPayload(
                ticketId,
                "TCK-1002",
                assigneeId,
                null);

        assertEquals(ticketId, payload.ticketId());
        assertEquals(assigneeId, payload.assigneeId());
        assertEquals(null, payload.assignedTeamId());
    }

    @Test
    void createsExternalCommentPayloadWithoutCommentBody() {
        UUID commentId = UUID.randomUUID();

        ExternalCommentAddedPayload payload = new ExternalCommentAddedPayload(
                UUID.randomUUID(),
                "TCK-1003",
                commentId,
                UUID.randomUUID());

        assertEquals(commentId, payload.commentId());
    }

    @Test
    void rejectsInvalidWorklogDuration() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> new WorklogAddedPayload(
                id,
                "TCK-1004",
                id,
                id,
                LocalDate.parse("2026-05-27"),
                0));
    }
}
