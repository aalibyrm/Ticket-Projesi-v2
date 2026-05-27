package com.ticketmanagement.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeTests {

    @Test
    void createsEnvelopeFromEventType() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-05-27T10:15:30Z");

        EventEnvelope<TestPayload> envelope = EventEnvelope.of(
                EventType.TICKET_CREATED,
                actorId,
                ticketId,
                "request-123",
                new TestPayload(ticketId),
                Clock.fixed(occurredAt, ZoneOffset.UTC));

        assertNotNull(envelope.eventId());
        assertEquals("ticket.created", envelope.eventType());
        assertEquals(1, envelope.version());
        assertEquals(occurredAt, envelope.occurredAt());
        assertEquals(actorId, envelope.actorId());
        assertEquals("ticket", envelope.aggregateType());
        assertEquals(ticketId, envelope.aggregateId());
        assertEquals("request-123", envelope.correlationId());
        assertEquals(ticketId, envelope.payload().ticketId());
    }

    @Test
    void normalizesBlankCorrelationIdToNull() {
        EventEnvelope<TestPayload> envelope = EventEnvelope.of(
                EventType.TICKET_CREATED,
                UUID.randomUUID(),
                UUID.randomUUID(),
                " ",
                new TestPayload(UUID.randomUUID()));

        assertNull(envelope.correlationId());
    }

    @Test
    void rejectsInvalidRequiredEnvelopeFields() {
        UUID id = UUID.randomUUID();
        TestPayload payload = new TestPayload(id);

        assertThrows(IllegalArgumentException.class, () -> new EventEnvelope<>(
                UUID.randomUUID(),
                " ",
                1,
                Instant.now(),
                id,
                "ticket",
                id,
                null,
                payload));

        assertThrows(IllegalArgumentException.class, () -> new EventEnvelope<>(
                UUID.randomUUID(),
                "ticket.created",
                2,
                Instant.now(),
                id,
                "ticket",
                id,
                null,
                payload));
    }

    private record TestPayload(UUID ticketId) implements EventPayload {
    }
}
