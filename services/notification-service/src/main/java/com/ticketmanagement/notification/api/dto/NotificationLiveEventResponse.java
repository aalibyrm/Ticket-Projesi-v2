package com.ticketmanagement.notification.api.dto;

import java.time.OffsetDateTime;

public record NotificationLiveEventResponse(
        String eventType,
        NotificationResponse notification,
        OffsetDateTime emittedAt) {
}
