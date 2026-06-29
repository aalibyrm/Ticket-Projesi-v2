package com.ticketmanagement.ticket.application;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ActorProfileDirectory {

    private static final Map<UUID, ActorProfile> PROFILES = Map.ofEntries(
            profile("80000000-0000-0000-0000-000000000001", "Ayse Yilmaz", "ayse.yilmaz@example.local"),
            profile("80000000-0000-0000-0000-000000000004", "Mehmet Demir", "mehmet.demir@example.local"),
            profile("80000000-0000-0000-0000-000000000005", "Zeynep Kaya", "zeynep.kaya@example.local"),
            profile("80000000-0000-0000-0000-000000000006", "Emre Arslan", "emre.arslan@example.local"),
            profile("80000000-0000-0000-0000-000000000007", "Ceren Aksoy", "ceren.aksoy@example.local"),
            profile("30000000-0000-0000-0000-000000000001", "Irem Gunes", "irem.gunes@example.local"),
            profile("30000000-0000-0000-0000-000000000002", "Cem Arslan", "cem.arslan@example.local"),
            profile("30000000-0000-0000-0000-000000000003", "Seda Yildirim", "seda.yildirim@example.local"),
            profile("30000000-0000-0000-0000-000000000004", "Okan Demir", "okan.demir@example.local"),
            profile("30000000-0000-0000-0000-000000000005", "Derya Korkmaz", "derya.korkmaz@example.local"),
            profile("30000000-0000-0000-0000-000000000006", "Alp Kaya", "alp.kaya@example.local"),
            profile("30000000-0000-0000-0000-000000000007", "Melis Acar", "melis.acar@example.local"),
            profile("30000000-0000-0000-0000-000000000008", "Bora Yalcin", "bora.yalcin@example.local"),
            profile("40000000-0000-0000-0000-000000000001", "Elif Aydin", "elif.aydin@example.local"),
            profile("40000000-0000-0000-0000-000000000002", "Mert Kaya", "mert.kaya@example.local"),
            profile("40000000-0000-0000-0000-000000000003", "Deniz Arslan", "deniz.arslan@example.local"),
            profile("40000000-0000-0000-0000-000000000004", "Selin Demir", "selin.demir@example.local"),
            profile("40000000-0000-0000-0000-000000000005", "Baran Yilmaz", "baran.yilmaz@example.local"),
            profile("40000000-0000-0000-0000-000000000006", "Ece Sahin", "ece.sahin@example.local"),
            profile("40000000-0000-0000-0000-000000000007", "Onur Demir", "onur.demir@example.local"),
            profile("40000000-0000-0000-0000-000000000008", "Zeynep Ozturk", "zeynep.ozturk@example.local"),
            profile("80000000-0000-0000-0000-000000000002", "Deniz Karaca", "deniz.karaca@example.local"),
            profile("80000000-0000-0000-0000-000000000003", "Burak Ozkan", "burak.ozkan@example.local"));

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
