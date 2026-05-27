package com.ticketmanagement.ticket.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.AddWorklogRequest;
import com.ticketmanagement.ticket.api.dto.AssignTicketRequest;
import com.ticketmanagement.ticket.api.dto.ChangeTicketStatusRequest;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketWorklogEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketWorklogJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketAgentCommandService {

    private final TicketJpaRepository ticketRepository;
    private final TicketCommentJpaRepository ticketCommentRepository;
    private final TicketWorklogJpaRepository ticketWorklogRepository;
    private final TicketMapper ticketMapper;
    private final TicketOutboxService ticketOutboxService;
    private final TicketWorkflowPort ticketWorkflowPort;

    // Agent tarafindan ticket status bilgisini degistirir ve event uretir.
    @Transactional
    public TicketResponse changeStatus(UUID actorId, UUID ticketId, ChangeTicketStatusRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        TicketStatusTransition transition = ticketWorkflowPort.authorizeStatusTransition(
                ticketId,
                ticket.getStatus(),
                request.status());

        ticket.changeStatus(transition.newStatus());
        ticketOutboxService.saveTicketStatusChanged(
                ticket,
                actorId,
                transition.previousStatus(),
                transition.newStatus());
        return ticketMapper.toResponse(ticket);
    }

    // Agent veya ekip atamasini ticket uzerinde gunceller ve event uretir.
    @Transactional
    public TicketResponse assignTicket(UUID actorId, UUID ticketId, AssignTicketRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);

        ticket.assignTo(request.assigneeId(), request.assignedTeamId());
        ticketOutboxService.saveTicketAssigned(ticket, actorId);
        return ticketMapper.toResponse(ticket);
    }

    // Musterinin de gorebilecegi external yorumu ticket'a ekler ve event uretir.
    @Transactional
    public TicketCommentResponse addExternalComment(UUID actorId, UUID ticketId, AddExternalCommentRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        TicketCommentEntity comment = ticketCommentRepository.save(TicketCommentEntity.external(
                UUID.randomUUID(),
                ticket,
                actorId,
                request.body().trim()));

        ticketOutboxService.saveExternalCommentAdded(comment, actorId);
        return ticketMapper.toResponse(comment);
    }

    // Agent'in harcadigi sureyi worklog olarak kaydeder ve event uretir.
    @Transactional
    public TicketWorklogResponse addWorklog(UUID actorId, UUID ticketId, AddWorklogRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        TicketWorklogEntity worklog = ticketWorklogRepository.save(TicketWorklogEntity.create(
                UUID.randomUUID(),
                ticket,
                actorId,
                request.workDate(),
                request.durationMinutes(),
                request.description().trim()));

        ticketOutboxService.saveWorklogAdded(worklog, actorId);
        return ticketMapper.toResponse(worklog);
    }

    // Guncelleme islemleri icin ticket kaydini pessimistic lock ile getirir.
    private TicketEntity findTicketForUpdate(UUID ticketId) {
        return ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));
    }
}
