package com.ticketmanagement.ticket.api.dto;

import jakarta.validation.constraints.NotNull;

import com.ticketmanagement.ticket.domain.TicketStatus;

public record ChangeTicketStatusRequest(
        @NotNull
        TicketStatus status) {
}
