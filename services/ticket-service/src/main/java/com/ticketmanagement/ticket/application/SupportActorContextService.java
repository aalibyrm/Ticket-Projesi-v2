package com.ticketmanagement.ticket.application;

import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TeamMemberJpaRepository;

@Service
@RequiredArgsConstructor
public class SupportActorContextService {

    private final TeamMemberJpaRepository teamMemberRepository;

    // Support actor icin DB kaynakli ekip uyeligi ve liderlik context'i olusturur.
    @Transactional(readOnly = true)
    public SupportActorContext resolve(UUID actorId, Set<String> roles) {
        List<TeamMemberEntity> memberships = teamMemberRepository.findActiveMembershipsForActor(actorId);
        Set<UUID> memberTeamIds = memberships.stream()
                .map(member -> member.getTeam().getId())
                .collect(Collectors.toUnmodifiableSet());
        Set<UUID> leadTeamIds = memberships.stream()
                .filter(TeamMemberEntity::isTeamLead)
                .map(member -> member.getTeam().getId())
                .collect(Collectors.toUnmodifiableSet());

        return new SupportActorContext(actorId, roles, memberTeamIds, leadTeamIds);
    }
}
