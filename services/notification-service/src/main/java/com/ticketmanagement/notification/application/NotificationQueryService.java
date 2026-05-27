package com.ticketmanagement.notification.application;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.notification.api.dto.NotificationResponse;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationJpaRepository;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationJpaRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // Kullaniciya ait notification kayitlarini read filtresiyle listeler.
    @Transactional(readOnly = true)
    public List<NotificationResponse> listUserNotifications(UUID userId, Boolean read) {
        List<NotificationEntity> notifications = read == null
                ? notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId)
                : notificationRepository.findByRecipientIdAndReadOrderByCreatedAtDesc(userId, read);
        return notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    // Kullaniciya ait notification kaydini okundu olarak isaretler.
    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.markRead();
        return notificationMapper.toResponse(notification);
    }
}
