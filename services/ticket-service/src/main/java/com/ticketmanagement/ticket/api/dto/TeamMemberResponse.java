package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

public record TeamMemberResponse(
        UUID actorId,
        UUID teamId,
        String teamCode,
        boolean teamLead) {
}
