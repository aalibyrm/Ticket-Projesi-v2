package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

public record TicketTopicResponse(
        UUID id,
        String code,
        String name,
        String description) {
}
