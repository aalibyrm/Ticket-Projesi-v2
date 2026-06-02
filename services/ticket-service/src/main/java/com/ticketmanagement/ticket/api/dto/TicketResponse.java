package com.ticketmanagement.ticket.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
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
        String topicCode,
        String topicName,
        UUID routedDepartmentId,
        String routedDepartmentCode,
        String routedDepartmentName,
        String summary,
        String description,
        TicketPriority priority,
        TicketStatus status,
        UUID assigneeId,
        UUID assignedTeamId,
        List<TicketAttachmentResponse> attachments,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
