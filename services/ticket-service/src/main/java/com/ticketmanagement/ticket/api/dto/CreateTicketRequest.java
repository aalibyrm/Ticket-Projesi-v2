package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.ticketmanagement.ticket.domain.TicketPriority;

public record CreateTicketRequest(
        @NotNull UUID productId,
        @NotBlank @Size(max = 80) String topicCode,
        @NotBlank @Size(max = 180) String summary,
        @NotBlank @Size(max = 5000) String description,
        TicketPriority priority) {
}
