package com.ticketmanagement.notification.application;

import org.springframework.stereotype.Component;

import com.ticketmanagement.notification.api.dto.NotificationResponse;
import com.ticketmanagement.notification.infrastructure.persistence.NotificationEntity;

@Component
class NotificationMapper {

    NotificationResponse toResponse(NotificationEntity entity) {
        return new NotificationResponse(
                entity.getId(),
                entity.getType().name(),
                entity.getTitle(),
                entity.getMessage(),
                entity.isRead(),
                entity.getCreatedAt());
    }
}
