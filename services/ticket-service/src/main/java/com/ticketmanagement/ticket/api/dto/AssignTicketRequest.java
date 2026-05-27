package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignTicketRequest(
        @NotNull
        UUID assigneeId,

        UUID assignedTeamId) {
}
