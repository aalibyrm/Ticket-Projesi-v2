package com.ticketmanagement.ticket.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.ticket.domain.TicketCommentVisibility;

public record TicketCommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorId,
        TicketCommentVisibility visibility,
        String body,
        OffsetDateTime createdAt) {
}
