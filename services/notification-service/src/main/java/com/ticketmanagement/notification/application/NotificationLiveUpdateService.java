package com.ticketmanagement.notification.application;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ticketmanagement.notification.api.dto.NotificationLiveEventResponse;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;

@Service
@RequiredArgsConstructor
public class NotificationLiveUpdateService {

    private static final String CONNECTED = "stream.connected";
    private static final String HEARTBEAT = "stream.heartbeat";
    private static final String NOTIFICATION_CREATED = "notification.created";
    private static final String NOTIFICATION_READ = "notification.read";

    private final NotificationMapper notificationMapper;
    private final ConcurrentMap<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    @Value("${app.live-updates.sse.timeout-ms:1800000}")
    private long timeoutMillis;

    // Kullaniciya ait SSE stream baglantisini kaydeder.
    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        userEmitters.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(ignored -> removeEmitter(userId, emitter));
        send(userId, emitter, connectedEvent());
        return emitter;
    }

    // Yeni notification olustugunda ilgili kullaniciya live event yollar.
    public void publishNotificationCreated(NotificationEntity notification) {
        publish(notification.getRecipientId(), liveEvent(NOTIFICATION_CREATED, notification));
    }

    // Notification okundu isaretlendiginde ilgili kullaniciya live event yollar.
    public void publishNotificationRead(NotificationEntity notification) {
        publish(notification.getRecipientId(), liveEvent(NOTIFICATION_READ, notification));
    }

    // Acik SSE baglantilarini proxy timeout'larina karsi canli tutar.
    @Scheduled(fixedDelayString = "${app.live-updates.sse.heartbeat-ms:25000}")
    public void sendHeartbeat() {
        NotificationLiveEventResponse event = new NotificationLiveEventResponse(HEARTBEAT, null, now());
        userEmitters.forEach((userId, emitters) -> emitters.forEach(emitter -> send(userId, emitter, event)));
    }

    // Testler ve operational insight icin aktif kullanici emitter sayisini dondurur.
    int activeEmitterCount(UUID userId) {
        return userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).size();
    }

    // Bir kullanicinin tum acik emitter'larina event yollar.
    private void publish(UUID userId, NotificationLiveEventResponse event) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        emitters.forEach(emitter -> send(userId, emitter, event));
    }

    // Tek emitter'a SSE event yollar; hata alan emitter'i registry'den temizler.
    private void send(UUID userId, SseEmitter emitter, NotificationLiveEventResponse event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.eventType())
                    .id(UUID.randomUUID().toString())
                    .data(event));
        } catch (IOException | IllegalStateException exception) {
            removeEmitter(userId, emitter);
        }
    }

    // Emitter'i kullanici registry'sinden temizler.
    private void removeEmitter(UUID userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            userEmitters.remove(userId, emitters);
        }
    }

    // Notification entity'sini frontend tarafinda cache invalidation icin kullanilan live event'e cevirir.
    private NotificationLiveEventResponse liveEvent(String eventType, NotificationEntity notification) {
        return new NotificationLiveEventResponse(eventType, notificationMapper.toResponse(notification), now());
    }

    // Stream baglantisi basarili acildiginda ilk handshake eventini uretir.
    private static NotificationLiveEventResponse connectedEvent() {
        return new NotificationLiveEventResponse(CONNECTED, null, now());
    }

    // Event timestamp'leri icin UTC zaman uretir.
    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
