package com.ticketmanagement.reporting.api.dto;

import java.util.UUID;

public record DepartmentTicketCountResponse(
        UUID routedDepartmentId,
        String routedDepartmentCode,
        String routedDepartmentName,
        long count) {
}
