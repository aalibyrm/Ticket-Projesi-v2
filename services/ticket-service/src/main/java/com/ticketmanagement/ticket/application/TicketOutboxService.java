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
import com.ticketmanagement.event.EventPayload;
import com.ticketmanagement.event.ticket.ExternalCommentAddedPayload;
import com.ticketmanagement.event.ticket.TicketAssignedPayload;
import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.event.ticket.TicketStatusChangedPayload;
import com.ticketmanagement.event.ticket.WorklogAddedPayload;
import com.ticketmanagement.ticket.infrastructure.persistence.DepartmentEntity;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxEventEntity;
import com.ticketmanagement.ticket.infrastructure.outbox.OutboxEventJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketTopicEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketWorklogEntity;
import com.ticketmanagement.ticket.domain.TicketStatus;

@Service
@RequiredArgsConstructor
public class TicketOutboxService {

    private final ObjectMapper objectMapper;
    private final OutboxEventJpaRepository outboxEventRepository;

    // Yeni ticket olusturma eventini mevcut transaction icinde outbox'a kaydeder.
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveTicketCreated(TicketEntity ticket, UUID actorId) {
        TicketTopicEntity topic = ticket.getTopic();
        DepartmentEntity routedDepartment = ticket.getRoutedDepartment();
        TicketCreatedPayload payload = new TicketCreatedPayload(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getCustomerId(),
                ticket.getProduct().getId(),
                topic == null ? null : topic.getCode(),
                topic == null ? null : topic.getName(),
                routedDepartment == null ? null : routedDepartment.getId(),
                routedDepartment == null ? null : routedDepartment.getCode(),
                routedDepartment == null ? null : routedDepartment.getName(),
                ticket.getPriority().name(),
                ticket.getStatus().name());
        EventEnvelope<TicketCreatedPayload> envelope = EventEnvelope.of(
                EventType.TICKET_CREATED,
                actorId,
                ticket.getId(),
                payload);
        JsonNode payloadJson = objectMapper.valueToTree(payload);

        savePending(EventType.TICKET_CREATED, envelope, payloadJson);
    }

    // Ticket status degisikligi eventini mevcut transaction icinde outbox'a kaydeder.
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveTicketStatusChanged(
            TicketEntity ticket,
            UUID actorId,
            TicketStatus previousStatus,
            TicketStatus newStatus) {
        TicketStatusChangedPayload payload = new TicketStatusChangedPayload(
                ticket.getId(),
                ticket.getTicketNumber(),
                previousStatus.name(),
                newStatus.name());
        saveEvent(EventType.TICKET_STATUS_CHANGED, actorId, ticket.getId(), payload);
    }

    // Ticket assignment eventini mevcut transaction icinde outbox'a kaydeder.
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveTicketAssigned(TicketEntity ticket, UUID actorId) {
        TicketAssignedPayload payload = new TicketAssignedPayload(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getAssigneeId(),
                ticket.getAssignedTeamId());
        saveEvent(EventType.TICKET_ASSIGNED, actorId, ticket.getId(), payload);
    }

    // External yorum eventini mevcut transaction icinde outbox'a kaydeder.
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveExternalCommentAdded(TicketCommentEntity comment, UUID actorId) {
        TicketEntity ticket = comment.getTicket();
        ExternalCommentAddedPayload payload = new ExternalCommentAddedPayload(
                ticket.getId(),
                ticket.getTicketNumber(),
                comment.getId(),
                comment.getAuthorId());
        saveEvent(EventType.TICKET_EXTERNAL_COMMENT_ADDED, actorId, ticket.getId(), payload);
    }

    // Worklog eventini mevcut transaction icinde outbox'a kaydeder.
    @Transactional(propagation = Propagation.MANDATORY)
    public void saveWorklogAdded(TicketWorklogEntity worklog, UUID actorId) {
        TicketEntity ticket = worklog.getTicket();
        WorklogAddedPayload payload = new WorklogAddedPayload(
                ticket.getId(),
                ticket.getTicketNumber(),
                worklog.getId(),
                worklog.getAgentId(),
                worklog.getWorkDate(),
                worklog.getDurationMinutes());
        saveEvent(EventType.TICKET_WORKLOG_ADDED, actorId, ticket.getId(), payload);
    }

    // Event payload'ini envelope ile birlikte outbox kaydina hazirlar.
    private <T extends EventPayload> void saveEvent(EventType eventType, UUID actorId, UUID aggregateId, T payload) {
        EventEnvelope<T> envelope = EventEnvelope.of(
                eventType,
                actorId,
                aggregateId,
                payload);
        JsonNode payloadJson = objectMapper.valueToTree(payload);

        savePending(eventType, envelope, payloadJson);
    }

    // Hazirlanan event envelope'unu pending outbox kaydi olarak saklar.
    private void savePending(EventType eventType, EventEnvelope<?> envelope, JsonNode payloadJson) {
        outboxEventRepository.save(OutboxEventEntity.pending(
                eventType.topic(),
                envelope,
                payloadJson));
    }
}
