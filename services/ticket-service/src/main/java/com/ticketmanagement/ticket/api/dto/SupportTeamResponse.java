package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

public record SupportTeamResponse(
        UUID id,
        UUID departmentId,
        String departmentCode,
        String code,
        String name,
        UUID leadActorId) {
}
