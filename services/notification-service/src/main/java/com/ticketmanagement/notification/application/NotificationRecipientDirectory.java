package com.ticketmanagement.notification.application;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class NotificationRecipientDirectory {

    private static final Map<UUID, NotificationRecipientProfile> PROFILES = Map.ofEntries(
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

    // Actor kimligini e-posta teslimati icin okunabilir demo profile cevirir.
    public NotificationRecipientProfile resolve(UUID actorId) {
        NotificationRecipientProfile profile = PROFILES.get(actorId);
        if (profile != null) {
            return profile;
        }
        String shortId = actorId.toString().substring(0, 8).toLowerCase(Locale.ROOT);
        return new NotificationRecipientProfile("User " + shortId, "user-" + shortId + "@example.local");
    }

    private static Map.Entry<UUID, NotificationRecipientProfile> profile(
            String actorId,
            String displayName,
            String email) {
        return Map.entry(UUID.fromString(actorId), new NotificationRecipientProfile(displayName, email));
    }

    public record NotificationRecipientProfile(
            String displayName,
            String email) {
    }
}
