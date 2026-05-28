package com.ticketmanagement.reporting.api.dto;

public record ClosedTicketPriorityCountResponse(
        String priority,
        long count) {
}
