package com.ticketmanagement.ticket.application;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record SupportActorContext(
        UUID actorId,
        Set<String> roles,
        Set<UUID> memberTeamIds,
        Set<UUID> leadTeamIds) {

    public SupportActorContext {
        roles = Set.copyOf(roles);
        memberTeamIds = Set.copyOf(memberTeamIds);
        leadTeamIds = Set.copyOf(leadTeamIds);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean isMemberOfTeam(UUID teamId) {
        return teamId != null && memberTeamIds.contains(teamId);
    }

    public boolean isLeadOfTeam(UUID teamId) {
        return teamId != null && leadTeamIds.contains(teamId);
    }

    public Set<UUID> readableTeamIds() {
        return Stream.concat(memberTeamIds.stream(), leadTeamIds.stream())
                .collect(Collectors.toUnmodifiableSet());
    }
}
