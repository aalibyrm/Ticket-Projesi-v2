package com.ticketmanagement.ticket.application;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.event.EventEnvelope;
import com.ticketmanagement.event.EventType;
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxEventEntity;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxEventJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;

@Service
@RequiredArgsConstructor
public class TicketOutboxService {

    private final ObjectMapper objectMapper;
    private final OutboxEventJpaRepository outboxEventRepository;

    // Yeni ticket olusturma eventini mevcut transaction icinde outbox'a kaydeder.
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveTicketCreated(TicketEntity ticket, UUID actorId) {
        TicketCreatedPayload payload = new TicketCreatedPayload(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getCustomerId(),
                ticket.getProduct().getId(),
                ticket.getPriority().name(),
                ticket.getStatus().name());
        EventEnvelope<TicketCreatedPayload> envelope = EventEnvelope.of(
                EventType.TICKET_CREATED,
                actorId,
                ticket.getId(),
                payload);
        JsonNode payloadJson = objectMapper.valueToTree(payload);

        outboxEventRepository.save(OutboxEventEntity.pending(
                EventType.TICKET_CREATED.topic(),
                envelope,
                payloadJson));
    }
}
