package com.ticketmanagement.workflow.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.workflow.domain.SlaPriority;
import com.ticketmanagement.workflow.infrastructure.kafka.ConsumedEvent;

@Service
@RequiredArgsConstructor
public class TicketEventSlaService {

    static final String CONSUMER_NAME = "workflow-sla-service.ticket-events";
    private static final String TICKET_CREATED = "ticket.created";

    private final ObjectMapper objectMapper;
    private final ConsumerIdempotencyService consumerIdempotencyService;
    private final SlaTicketStateService slaTicketStateService;

    // Ticket eventini idempotent sekilde SLA state hesaplamasina cevirir.
    public boolean handleTicketEvent(ConsumedEvent event) {
        if (!TICKET_CREATED.equals(event.eventType())) {
            return false;
        }
        return consumerIdempotencyService.processOnce(
                CONSUMER_NAME,
                event,
                () -> createSlaState(event));
    }

    // TicketCreated payload'indan priority bazli deadline hesaplayip state kaydeder.
    private void createSlaState(ConsumedEvent event) {
        TicketCreatedPayload payload = readTicketCreatedPayload(event);
        slaTicketStateService.createStateForTicketCreated(new TicketSlaCreationCommand(
                payload.ticketId(),
                payload.ticketNumber(),
                SlaPriority.from(payload.priority()),
                event.occurredAt()));
    }

    // TicketCreated payload JSON'unu ortak event contract tipine cevirir.
    private TicketCreatedPayload readTicketCreatedPayload(ConsumedEvent event) {
        try {
            return objectMapper.treeToValue(event.payload(), TicketCreatedPayload.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid ticket.created payload", exception);
        }
    }
}
