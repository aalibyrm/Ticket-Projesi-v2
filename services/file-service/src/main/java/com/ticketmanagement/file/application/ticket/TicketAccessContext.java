package com.ticketmanagement.file.application.ticket;

import java.util.Set;
import java.util.UUID;

public record TicketAccessContext(
        UUID actorId,
        Set<String> roles,
        String bearerToken) {
}
