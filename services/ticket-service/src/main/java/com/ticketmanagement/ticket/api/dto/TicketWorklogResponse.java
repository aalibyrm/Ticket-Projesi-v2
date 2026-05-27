package com.ticketmanagement.ticket.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TicketWorklogResponse(
        UUID id,
        UUID ticketId,
        UUID agentId,
        LocalDate workDate,
        int durationMinutes,
        String description,
        OffsetDateTime createdAt) {
}
