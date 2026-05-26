package com.ticketmanagement.ticket.api.dto;

import java.util.UUID;

public record AttachmentAccessResponse(
        UUID ticketId,
        UUID actorId,
        boolean uploadAllowed,
        boolean downloadAllowed) {
}
