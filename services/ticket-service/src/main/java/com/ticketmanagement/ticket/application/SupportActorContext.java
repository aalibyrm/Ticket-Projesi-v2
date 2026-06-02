package com.ticketmanagement.ticket.application;

import java.util.Set;
import java.util.UUID;

public record SupportActorContext(
        UUID actorId,
        Set<String> roles,
        Set<UUID> teamIds) {

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
