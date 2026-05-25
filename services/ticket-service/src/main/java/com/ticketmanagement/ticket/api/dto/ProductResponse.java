package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

public record ProductResponse(UUID id, String code, String name) {
}

