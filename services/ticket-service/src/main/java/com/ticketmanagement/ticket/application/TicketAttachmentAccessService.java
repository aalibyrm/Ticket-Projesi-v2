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
    private static final String ROLE_CUSTOMER = "CUSTOMER";

    private final TicketJpaRepository ticketRepository;

    // Dosya ekleme/indirme icin ticket sahipligi ve rol yetkisini dogrular.
    @Transactional(readOnly = true)
    public AttachmentAccessResponse assertAttachmentAccess(UUID ticketId, UUID actorId, Set<String> roles) {
        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        if (roles.contains(ROLE_ADMIN) || canCustomerAccess(ticket, actorId, roles)) {
            return new AttachmentAccessResponse(ticketId, actorId, true, true);
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Customer rolundeki actor'un sadece kendi ticket'ina erismesini saglar.
    private boolean canCustomerAccess(TicketEntity ticket, UUID actorId, Set<String> roles) {
        return roles.contains(ROLE_CUSTOMER) && ticket.getCustomerId().equals(actorId);
    }
}
