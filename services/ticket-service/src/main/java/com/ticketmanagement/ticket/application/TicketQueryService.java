package com.ticketmanagement.ticket.application;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.TicketAgentSummaryResponse;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.domain.TicketCommentVisibility;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketQueryService {

    private final TicketCommentJpaRepository ticketCommentRepository;
    private final TicketJpaRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final TicketAttachmentPort ticketAttachmentPort;
    private final ActorProfileDirectory actorProfileDirectory;
    private final AgentSummaryLookupPort agentSummaryLookupPort;

    // Musterinin kendi ticket listesini en yeni kayitlar once gelecek sekilde getirir.
    @Transactional(readOnly = true)
    public List<TicketResponse> listTicketsForCustomer(UUID customerId) {
        return ticketRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // Musterinin sadece kendisine ait ticket detayini getirir.
    @Transactional(readOnly = true)
    public TicketResponse getTicketForCustomer(UUID customerId, UUID ticketId, AttachmentLookupContext context) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (!ticket.getCustomerId().equals(customerId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        return ticketMapper.toResponse(ticket, ticketAttachmentPort.listAttachments(ticketId, context));
    }

    // Musterinin kendi ticket'ina atanmis agent icin sinirli performans ozetini getirir.
    @Transactional(readOnly = true)
    public TicketAgentSummaryResponse getAgentSummaryForCustomer(UUID customerId, UUID ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (!ticket.getCustomerId().equals(customerId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        UUID assigneeId = ticket.getAssigneeId();
        if (assigneeId == null) {
            return TicketAgentSummaryResponse.unassigned(ticket.getAssignedTeamId());
        }

        ActorProfileDirectory.ActorProfile profile = actorProfileDirectory.findByActorId(assigneeId)
                .orElse(new ActorProfileDirectory.ActorProfile("Destek Temsilcisi", null));
        return TicketAgentSummaryResponse.assigned(
                agentSummaryLookupPort.getAgentSummary(assigneeId),
                profile.displayName(),
                profile.email(),
                ticket.getAssignedTeamId());
    }

    // Musteriye sadece kendi ticket'ina ait external yorumlari dondurur.
    @Transactional(readOnly = true)
    public List<TicketCommentResponse> listExternalCommentsForCustomer(UUID customerId, UUID ticketId) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (!ticket.getCustomerId().equals(customerId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        return ticketCommentRepository.findByTicketIdAndVisibilityOrderByCreatedAtAsc(
                        ticketId,
                        TicketCommentVisibility.EXTERNAL)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }
}
