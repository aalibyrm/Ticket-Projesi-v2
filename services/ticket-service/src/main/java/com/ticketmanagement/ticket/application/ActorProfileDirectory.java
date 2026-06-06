package com.ticketmanagement.ticket.application;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ActorProfileDirectory {

    private static final Map<UUID, ActorProfile> PROFILES = Map.ofEntries(
            profile("80000000-0000-0000-0000-000000000001", "Demo Customer", "customer.user@example.local"),
            profile("30000000-0000-0000-0000-000000000001", "Identity Lead", "lead.identity@example.local"),
            profile("30000000-0000-0000-0000-000000000002", "Permission Lead", "lead.permission@example.local"),
            profile("30000000-0000-0000-0000-000000000003", "Web Lead", "lead.web@example.local"),
            profile("30000000-0000-0000-0000-000000000004", "Core Lead", "lead.core@example.local"),
            profile("30000000-0000-0000-0000-000000000005", "Network Lead", "lead.network@example.local"),
            profile("30000000-0000-0000-0000-000000000006", "Platform Lead", "lead.platform@example.local"),
            profile("30000000-0000-0000-0000-000000000007", "Billing Lead", "lead.billing@example.local"),
            profile("30000000-0000-0000-0000-000000000008", "Payment Lead", "lead.payment@example.local"),
            profile("40000000-0000-0000-0000-000000000001", "Identity Agent", "agent.identity@example.local"),
            profile("40000000-0000-0000-0000-000000000002", "Permission Agent", "agent.permission@example.local"),
            profile("40000000-0000-0000-0000-000000000003", "Web Agent", "agent.web@example.local"),
            profile("40000000-0000-0000-0000-000000000004", "Core Agent", "agent.core@example.local"),
            profile("40000000-0000-0000-0000-000000000005", "Network Agent", "agent.network@example.local"),
            profile("40000000-0000-0000-0000-000000000006", "Platform Agent", "agent.platform@example.local"),
            profile("40000000-0000-0000-0000-000000000007", "Billing Agent", "agent.billing@example.local"),
            profile("40000000-0000-0000-0000-000000000008", "Payment Agent", "agent.payment@example.local"),
            profile("80000000-0000-0000-0000-000000000002", "Demo Manager", "manager.user@example.local"),
            profile("80000000-0000-0000-0000-000000000003", "Demo Admin", "admin.user@example.local"));

    // Bilinen demo actor kimligi icin okunabilir profil bilgisini dondurur.
    public Optional<ActorProfile> findByActorId(UUID actorId) {
        return Optional.ofNullable(PROFILES.get(actorId));
    }

    private static Map.Entry<UUID, ActorProfile> profile(String actorId, String displayName, String email) {
        return Map.entry(UUID.fromString(actorId), new ActorProfile(displayName, email));
    }

    public record ActorProfile(
            String displayName,
            String email) {
    }
}
