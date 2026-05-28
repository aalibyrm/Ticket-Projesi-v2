package com.ticketmanagement.event.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SlaPayloadTests {

    @Test
    void slaRiskPayloadTrimsTextFields() {
        UUID ticketId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant detectedAt = Instant.parse("2026-05-28T10:00:00Z");

        SlaRiskDetectedPayload payload = new SlaRiskDetectedPayload(
                ticketId,
                " TCK-3701 ",
                recipientId,
                " HIGH ",
                detectedAt.plusSeconds(7200),
                detectedAt,
                " Deadline is approaching. ");

        assertEquals("TCK-3701", payload.ticketNumber());
        assertEquals("HIGH", payload.priority());
        assertEquals("Deadline is approaching.", payload.riskReason());
    }

    @Test
    void slaBreachPayloadRejectsBlankReason() {
        UUID ticketId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant detectedAt = Instant.parse("2026-05-28T10:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> new SlaBreachedPayload(
                ticketId,
                "TCK-3702",
                recipientId,
                "HIGH",
                detectedAt.minusSeconds(60),
                detectedAt,
                " "));
    }
}
