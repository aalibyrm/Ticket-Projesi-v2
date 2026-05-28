package com.ticketmanagement.reporting.api.dto;

public record TicketStatusCountResponse(
        String status,
        long count) {
}
