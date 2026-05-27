package com.ticketmanagement.ticket.application;

import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.api.dto.AttachmentAccessResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketJpaRepository;

@Service
@RequiredArgsConstructor
public class TicketAttachmentAccessService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_AGENT = "AGENT";
    private static final String ROLE_CUSTOMER = "CUSTOMER";

    private final TicketJpaRepository ticketRepository;

    // Dosya ekleme/indirme icin ticket sahipligi ve rol yetkisini dogrular.
    @Transactional(readOnly = true)
    public AttachmentAccessResponse assertAttachmentAccess(
            UUID ticketId,
            UUID actorId,
            Set<String> roles,
            Set<UUID> teamIds) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (roles.contains(ROLE_ADMIN)
                || canCustomerAccess(ticket, actorId, roles)
                || canAssignedAgentAccess(ticket, actorId, roles)
                || canAssignedTeamAccess(ticket, teamIds, roles)) {
            return new AttachmentAccessResponse(ticketId, actorId, true, true);
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Customer rolundeki actor'un sadece kendi ticket'ina erismesini saglar.
    private boolean canCustomerAccess(TicketEntity ticket, UUID actorId, Set<String> roles) {
        return roles.contains(ROLE_CUSTOMER) && ticket.getCustomerId().equals(actorId);
    }

    // Agent rolundeki actor'un sadece kendisine atanmis ticket'a erismesini saglar.
    private boolean canAssignedAgentAccess(TicketEntity ticket, UUID actorId, Set<String> roles) {
        return roles.contains(ROLE_AGENT) && actorId.equals(ticket.getAssigneeId());
    }

    // Agent rolundeki actor'un sadece ekiplerinden birine atanmis ticket'a erismesini saglar.
    private boolean canAssignedTeamAccess(TicketEntity ticket, Set<UUID> teamIds, Set<String> roles) {
        return roles.contains(ROLE_AGENT)
                && ticket.getAssignedTeamId() != null
                && teamIds.contains(ticket.getAssignedTeamId());
    }
}
