package com.ticketmanagement.ticket.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketAttachmentResponse(
        UUID id,
        UUID ticketId,
        String originalFilename,
        String contentType,
        long sizeBytes,
        String validationStatus,
        String uploadStatus,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt) {
}
