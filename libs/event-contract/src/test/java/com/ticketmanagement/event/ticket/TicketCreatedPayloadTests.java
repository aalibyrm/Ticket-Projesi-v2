package com.ticketmanagement.event.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TicketCreatedPayloadTests {

    @Test
    void createsMinimalTicketCreatedPayload() {
        UUID ticketId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        TicketCreatedPayload payload = new TicketCreatedPayload(
                ticketId,
                "TCK-1000",
                customerId,
                productId,
                "HIGH",
                "NEW");

        assertEquals(ticketId, payload.ticketId());
        assertEquals("TCK-1000", payload.ticketNumber());
        assertEquals(customerId, payload.customerId());
        assertEquals(productId, payload.productId());
        assertEquals("HIGH", payload.priority());
        assertEquals("NEW", payload.status());
    }

    @Test
    void createsOrganizationAwareTicketCreatedPayload() {
        UUID ticketId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID teamId = UUID.randomUUID();
        UUID routedSupportActorId = UUID.randomUUID();

        TicketCreatedPayload payload = new TicketCreatedPayload(
                ticketId,
                "TCK-1001",
                customerId,
                productId,
                " WEB_PORTAL_BUG ",
                "Web Portal Bug",
                departmentId,
                "APPLICATION_SUPPORT",
                "Application Support",
                teamId,
                "WEB_APP_SUPPORT",
                "Web App Support",
                routedSupportActorId,
                "MEDIUM",
                "NEW");

        assertEquals("WEB_PORTAL_BUG", payload.topicCode());
        assertEquals("Web Portal Bug", payload.topicName());
        assertEquals(departmentId, payload.routedDepartmentId());
        assertEquals("APPLICATION_SUPPORT", payload.routedDepartmentCode());
        assertEquals("Application Support", payload.routedDepartmentName());
        assertEquals(teamId, payload.assignedTeamId());
        assertEquals("WEB_APP_SUPPORT", payload.assignedTeamCode());
        assertEquals("Web App Support", payload.assignedTeamName());
        assertEquals(routedSupportActorId, payload.routedSupportActorId());
    }

    @Test
    void rejectsBlankRequiredTextFields() {
        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> new TicketCreatedPayload(
                id,
                " ",
                id,
                id,
                "HIGH",
                "NEW"));
    }
}
