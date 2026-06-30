package com.ticketmanagement.ticket.application;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.infrastructure.persistence.DepartmentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.SupportTeamEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketRoutingCursorEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketRoutingCursorJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketRoutingRuleEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketRoutingRuleJpaRepository;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketTopicEntity;

@Service
@RequiredArgsConstructor
class TicketRoutingService {

    private final TicketRoutingRuleJpaRepository ticketRoutingRuleRepository;
    private final TicketRoutingCursorJpaRepository ticketRoutingCursorRepository;
    private final TeamMemberJpaRepository teamMemberRepository;
    private final ActorProfileDirectory actorProfileDirectory;

    // Ticket topic code degerini aktif route adaylari icinden round-robin ile ekibe cozer.
    @Transactional
    TicketRoutingResolution resolveActiveRoute(String topicCode) {
        String normalizedTopicCode = normalizeTopicCode(topicCode);
        List<TicketRoutingRuleEntity> routes = ticketRoutingRuleRepository.findActiveRoutesForTopicCode(normalizedTopicCode);
        if (routes.isEmpty()) {
            throw NotFoundException.topic(normalizedTopicCode);
        }
        TicketRoutingRuleEntity rule = selectRoundRobinRoute(routes);
        return new TicketRoutingResolution(
                rule.getTopic(),
                rule.getDepartment(),
                rule.getTeam(),
                resolveSupportNotificationRecipient(rule.getTeam()));
    }

    private TicketRoutingRuleEntity selectRoundRobinRoute(List<TicketRoutingRuleEntity> routes) {
        if (routes.size() == 1) {
            return routes.getFirst();
        }

        UUID topicId = routes.getFirst().getTopic().getId();
        TicketRoutingCursorEntity cursor = ticketRoutingCursorRepository.findByTopicIdForUpdate(topicId)
                .orElseGet(() -> ticketRoutingCursorRepository.saveAndFlush(TicketRoutingCursorEntity.create(topicId)));
        TicketRoutingRuleEntity selectedRoute = routes.get(cursor.currentIndex(routes.size()));
        cursor.advance(routes.size());
        return selectedRoute;
    }

    // Routed team icindeki login olabilen agent'i notification hedefi olarak secer.
    private UUID resolveSupportNotificationRecipient(SupportTeamEntity team) {
        List<TeamMemberEntity> activeMembers = teamMemberRepository.findActiveMembersForActiveTeam(team.getId());
        return activeMembers.stream()
                .filter(member -> !member.isTeamLead())
                .filter(this::hasKnownProfile)
                .findFirst()
                .or(() -> activeMembers.stream()
                        .filter(member -> !member.isTeamLead())
                        .findFirst())
                .or(() -> activeMembers.stream()
                        .filter(this::hasKnownProfile)
                        .findFirst())
                .or(() -> activeMembers.stream().findFirst())
                .map(TeamMemberEntity::getActorId)
                .orElse(team.getLeadActorId());
    }

    private boolean hasKnownProfile(TeamMemberEntity member) {
        return actorProfileDirectory.findByActorId(member.getActorId()).isPresent();
    }

    private String normalizeTopicCode(String topicCode) {
        if (topicCode == null || topicCode.isBlank()) {
            return "";
        }
        return topicCode.trim().toUpperCase(Locale.ROOT);
    }
}

record TicketRoutingResolution(
        TicketTopicEntity topic,
        DepartmentEntity department,
        SupportTeamEntity team,
        UUID supportNotificationRecipientId) {
}
