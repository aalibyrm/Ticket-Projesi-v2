package com.ticketmanagement.ticket.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketWorklogJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketAgentQueryService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final TicketJpaRepository ticketRepository;
    private final TicketCommentJpaRepository ticketCommentRepository;
    private final TicketWorklogJpaRepository ticketWorklogRepository;
    private final TicketMapper ticketMapper;
    private final TicketAttachmentPort ticketAttachmentPort;
    private final TicketSupportAccessService ticketSupportAccessService;

    // Agent queue icin actor'a veya ekiplerine atanmis ticket listesini getirir.
    @Transactional(readOnly = true)
    public List<TicketResponse> listTicketsForSupportActor(SupportActorContext context) {
        List<TicketEntity> tickets = context.hasRole(ROLE_ADMIN)
                ? ticketRepository.findAllByOrderByUpdatedAtDesc()
                : listAssignedTickets(context);

        return tickets.stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // Agent detail ekrani icin ticket'i attachment metadata ile getirir.
    @Transactional(readOnly = true)
    public TicketResponse getTicketForSupportActor(
            SupportActorContext context,
            UUID ticketId,
            AttachmentLookupContext attachmentLookupContext) {
        TicketEntity ticket = findTicket(ticketId);
        ticketSupportAccessService.assertCanManageTicket(ticket, context);
        return ticketMapper.toResponse(ticket, ticketAttachmentPort.listAttachments(ticketId, attachmentLookupContext));
    }

    // Agent'in gorebilecegi internal ve external yorumlari kronolojik dondurur.
    @Transactional(readOnly = true)
    public List<TicketCommentResponse> listCommentsForSupportActor(SupportActorContext context, UUID ticketId) {
        TicketEntity ticket = findTicket(ticketId);
        ticketSupportAccessService.assertCanManageTicket(ticket, context);
        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // Agent'in gorebilecegi worklog kayitlarini en yeni once gelecek sekilde dondurur.
    @Transactional(readOnly = true)
    public List<TicketWorklogResponse> listWorklogsForSupportActor(SupportActorContext context, UUID ticketId) {
        TicketEntity ticket = findTicket(ticketId);
        ticketSupportAccessService.assertCanManageTicket(ticket, context);
        return ticketWorklogRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    // Actor ve ekip atamalarini tekil ticket listesine birlestirir.
    private List<TicketEntity> listAssignedTickets(SupportActorContext context) {
        List<TicketEntity> tickets = new ArrayList<>(
                ticketRepository.findByAssigneeIdOrderByUpdatedAtDesc(context.actorId()));
        if (!context.teamIds().isEmpty()) {
            tickets.addAll(ticketRepository.findByAssignedTeamIdInOrderByUpdatedAtDesc(
                    new ArrayList<>(context.teamIds())));
        }

        return tickets.stream()
                .collect(Collectors.toMap(
                        TicketEntity::getId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new))
                .values()
                .stream()
                .sorted(Comparator.comparing(TicketEntity::getUpdatedAt).reversed())
                .toList();
    }

    // Ticket kaydini getirir veya standart not found hatasi uretir.
    private TicketEntity findTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));
    }
}
