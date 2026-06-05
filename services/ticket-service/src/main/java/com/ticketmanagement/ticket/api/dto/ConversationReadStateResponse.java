package com.ticketmanagement.ticket.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationReadStateResponse(
        UUID ticketId,
        long unreadCount,
        OffsetDateTime lastReadAt) {
}
