package com.ticketmanagement.reporting.api.dto;

import java.util.UUID;

public record TeamTicketCountResponse(
        UUID assignedTeamId,
        long count) {
}
