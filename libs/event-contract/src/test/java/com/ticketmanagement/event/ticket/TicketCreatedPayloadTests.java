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
