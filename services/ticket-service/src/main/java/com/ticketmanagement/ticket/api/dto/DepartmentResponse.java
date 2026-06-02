package com.ticketmanagement.ticket.api.dto;

import java.util.List;
import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String code,
        String name,
        List<SupportTeamResponse> teams) {
}
