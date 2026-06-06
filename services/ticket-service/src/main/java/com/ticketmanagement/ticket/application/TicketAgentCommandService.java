package com.ticketmanagement.ticket.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.AddInternalNoteRequest;
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
    private final TicketSupportAccessService ticketSupportAccessService;

    // Agent tarafindan ticket status bilgisini degistirir ve event uretir.
    @Transactional
    public TicketResponse changeStatus(SupportActorContext context, UUID ticketId, ChangeTicketStatusRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        ticketSupportAccessService.assertCanManageTicket(ticket, context);
        TicketStatusTransition transition = ticketWorkflowPort.authorizeStatusTransition(
                ticketId,
                ticket.getStatus(),
                request.status());

        ticket.changeStatus(transition.newStatus());
        ticketOutboxService.saveTicketStatusChanged(
                ticket,
                context.actorId(),
                transition.previousStatus(),
                transition.newStatus());
        return ticketMapper.toResponse(ticket);
    }

    // Agent veya ekip atamasini ticket uzerinde gunceller ve event uretir.
    @Transactional
    public TicketResponse assignTicket(SupportActorContext context, UUID ticketId, AssignTicketRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        ticketSupportAccessService.assertCanAssignTicket(
                ticket,
                context,
                request.assigneeId(),
                request.assignedTeamId());

        ticket.assignTo(request.assigneeId(), request.assignedTeamId());
        ticketOutboxService.saveTicketAssigned(ticket, context.actorId());
        return ticketMapper.toResponse(ticket);
    }

    // Musterinin de gorebilecegi external yorumu ticket'a ekler ve event uretir.
    @Transactional
    public TicketCommentResponse addExternalComment(
            SupportActorContext context,
            UUID ticketId,
            AddExternalCommentRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        ticketSupportAccessService.assertCanManageTicket(ticket, context);
        TicketCommentEntity comment = ticketCommentRepository.save(TicketCommentEntity.external(
                UUID.randomUUID(),
                ticket,
                context.actorId(),
                request.body().trim()));

        ticketOutboxService.saveExternalCommentAdded(comment, context.actorId());
        return ticketMapper.toResponse(comment);
    }

    // Sadece support ekibinin gorecegi internal notu ticket'a ekler.
    @Transactional
    public TicketCommentResponse addInternalNote(
            SupportActorContext context,
            UUID ticketId,
            AddInternalNoteRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        ticketSupportAccessService.assertCanAddInternalNote(ticket, context);
        TicketCommentEntity comment = ticketCommentRepository.save(TicketCommentEntity.internal(
                UUID.randomUUID(),
                ticket,
                context.actorId(),
                request.body().trim()));

        return ticketMapper.toResponse(comment);
    }

    // Agent'in harcadigi sureyi worklog olarak kaydeder ve event uretir.
    @Transactional
    public TicketWorklogResponse addWorklog(SupportActorContext context, UUID ticketId, AddWorklogRequest request) {
        TicketEntity ticket = findTicketForUpdate(ticketId);
        ticketSupportAccessService.assertCanManageTicket(ticket, context);
        TicketWorklogEntity worklog = ticketWorklogRepository.save(TicketWorklogEntity.create(
                UUID.randomUUID(),
                ticket,
                context.actorId(),
                request.workDate(),
                request.durationMinutes(),
                request.description().trim()));

        ticketOutboxService.saveWorklogAdded(worklog, context.actorId());
        return ticketMapper.toResponse(worklog);
    }

    // Guncelleme islemleri icin ticket kaydini pessimistic lock ile getirir.
    private TicketEntity findTicketForUpdate(UUID ticketId) {
        return ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));
    }
}
