package com.ticketmanagement.ticket.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;

public record TicketResponse(
        UUID id,
        String ticketNumber,
        UUID customerId,
        UUID productId,
        String productCode,
        String productName,
        String summary,
        String description,
        TicketPriority priority,
        TicketStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}

