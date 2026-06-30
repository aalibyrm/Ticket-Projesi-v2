package com.ticketmanagement.ticket.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.ticketmanagement.ticket.infrastructure.persistence.SupportTeamJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;

@Service
@RequiredArgsConstructor
public class TicketSupportAccessService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_AGENT = "AGENT";
    private static final String ROLE_MANAGER = "MANAGER";
    private static final String ROLE_TEAM_LEAD = "TEAM_LEAD";

    private final SupportTeamJpaRepository supportTeamRepository;
    private final TeamMemberJpaRepository teamMemberRepository;

    // Support actor'un ticket'i okuma yetkisi olup olmadigini dogrular.
    public void assertCanReadTicket(TicketEntity ticket, SupportActorContext context) {
        if (canReadTicket(ticket, context)) {
            return;
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Support actor'un ticket'i yonetme yetkisi olup olmadigini dogrular.
    public void assertCanManageTicket(TicketEntity ticket, SupportActorContext context) {
        if (canManageTicket(ticket, context)) {
            return;
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Support actor'un atanmadan internal note ekleyip ekleyemeyecegini dogrular.
    public void assertCanAddInternalNote(TicketEntity ticket, SupportActorContext context) {
        if (canAddInternalNote(ticket, context)) {
            return;
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Worklog kayitlarina yalniz ticket'in mevcut assignee agent'inin erismesini saglar.
    public void assertCanAccessWorklog(TicketEntity ticket, SupportActorContext context) {
        if (canAssignedAgentAccess(ticket, context)) {
            return;
        }

        throw ForbiddenOperationException.accessDenied();
    }

    // Support actor'un ticket assignment islemini yapip yapamayacagini dogrular.
    public void assertCanAssignTicket(
            TicketEntity ticket,
            SupportActorContext context,
            UUID assigneeId,
            UUID assignedTeamId) {
        if (ticket.getAssignedTeamId() != null && !ticket.getAssignedTeamId().equals(assignedTeamId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        if (!context.hasRole(ROLE_ADMIN)
                && !canLeadAssignInsideOwnTeam(ticket, context, assignedTeamId)
                && !canSelfAssignOpenTeamTicket(ticket, context, assigneeId, assignedTeamId)) {
            throw ForbiddenOperationException.accessDenied();
        }

        assertActiveAssignmentTarget(assigneeId, assignedTeamId);
    }

    // Support actor'un ticket'i okuyup okuyamayacagini boolean olarak dondurur.
    public boolean canReadTicket(TicketEntity ticket, SupportActorContext context) {
        return context.hasRole(ROLE_ADMIN)
                || context.hasRole(ROLE_MANAGER)
                || canAssignedAgentAccess(ticket, context)
                || canAssignedTeamRead(ticket, context);
    }

    // Support actor'un ticket'i guncelleyip guncelleyemeyecegini boolean olarak dondurur.
    public boolean canManageTicket(TicketEntity ticket, SupportActorContext context) {
        return context.hasRole(ROLE_ADMIN)
                || canAssignedAgentAccess(ticket, context)
                || canTeamLeadManage(ticket, context);
    }

    // Support actor'un internal note ekleyip ekleyemeyecegini boolean olarak dondurur.
    public boolean canAddInternalNote(TicketEntity ticket, SupportActorContext context) {
        return context.hasRole(ROLE_ADMIN)
                || canAssignedTeamRead(ticket, context);
    }

    // Actor'un ticket uzerindeki mevcut assignee olup olmadigini kontrol eder.
    private boolean canAssignedAgentAccess(TicketEntity ticket, SupportActorContext context) {
        return hasSupportStaffRole(context) && context.actorId().equals(ticket.getAssigneeId());
    }

    // Actor'un ticket'in atandigi ekipte aktif uye olup olmadigini kontrol eder.
    private boolean canAssignedTeamRead(TicketEntity ticket, SupportActorContext context) {
        return hasSupportStaffRole(context) && context.isMemberOfTeam(ticket.getAssignedTeamId());
    }

    // Actor'un ticket'in atandigi ekibin aktif lideri olup olmadigini kontrol eder.
    private boolean canTeamLeadManage(TicketEntity ticket, SupportActorContext context) {
        return hasSupportStaffRole(context) && context.isLeadOfTeam(ticket.getAssignedTeamId());
    }

    // Team lead'in yalniz kendi liderlik ettigi ekip icine atama yapmasini saglar.
    private boolean canLeadAssignInsideOwnTeam(
            TicketEntity ticket,
            SupportActorContext context,
            UUID assignedTeamId) {
        return canTeamLeadManage(ticket, context) && context.isLeadOfTeam(assignedTeamId);
    }

    // Ekip uyesinin yalniz kendi ekibindeki bos ticket'i uzerine almasina izin verir.
    private boolean canSelfAssignOpenTeamTicket(
            TicketEntity ticket,
            SupportActorContext context,
            UUID assigneeId,
            UUID assignedTeamId) {
        return hasSupportStaffRole(context)
                && ticket.getAssigneeId() == null
                && context.actorId().equals(assigneeId)
                && assignedTeamId != null
                && assignedTeamId.equals(ticket.getAssignedTeamId())
                && context.isMemberOfTeam(assignedTeamId);
    }

    // Assignment hedefinin aktif ekip ve aktif ekip uyesi oldugunu dogrular.
    private void assertActiveAssignmentTarget(UUID assigneeId, UUID assignedTeamId) {
        if (assignedTeamId == null) {
            return;
        }
        boolean activeTeam = supportTeamRepository.findActiveTeamWithActiveDepartment(assignedTeamId).isPresent();
        boolean activeMembership = teamMemberRepository.existsActiveMembership(assigneeId, assignedTeamId);
        if (!activeTeam || !activeMembership) {
            throw InvalidTicketOperationException.invalidAssignmentTarget(assigneeId, assignedTeamId);
        }
    }

    // Support operasyon rollerini merkezi olarak kontrol eder.
    private boolean hasSupportStaffRole(SupportActorContext context) {
        return context.hasRole(ROLE_AGENT) || context.hasRole(ROLE_TEAM_LEAD);
    }
}
