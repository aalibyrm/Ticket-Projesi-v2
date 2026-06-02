package com.ticketmanagement.ticket.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;

@Service
public class TicketSupportAccessService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_AGENT = "AGENT";

    // Support actor'un ticket'i yonetme yetkisi olup olmadigini dogrular.
    public void assertCanManageTicket(TicketEntity ticket, SupportActorContext context) {
        if (context.hasRole(ROLE_ADMIN)
                || canAssignedAgentAccess(ticket, context.actorId(), context)
                || canAssignedTeamAccess(ticket, context)) {
            return;
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Actor'un ticket uzerindeki mevcut assignee olup olmadigini kontrol eder.
    private boolean canAssignedAgentAccess(TicketEntity ticket, UUID actorId, SupportActorContext context) {
        return context.hasRole(ROLE_AGENT) && actorId.equals(ticket.getAssigneeId());
    }

    // Actor'un ticket'in atandigi ekipte olup olmadigini kontrol eder.
    private boolean canAssignedTeamAccess(TicketEntity ticket, SupportActorContext context) {
        return context.hasRole(ROLE_AGENT)
                && ticket.getAssignedTeamId() != null
                && context.teamIds().contains(ticket.getAssignedTeamId());
    }
}
