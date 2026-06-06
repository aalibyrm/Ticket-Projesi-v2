package com.ticketmanagement.notification.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID ticketId,
        String type,
        String title,
        String message,
        boolean read,
        OffsetDateTime createdAt) {
}
