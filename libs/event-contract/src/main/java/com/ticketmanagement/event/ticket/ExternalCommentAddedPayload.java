package com.ticketmanagement.event.ticket;

import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record ExternalCommentAddedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID commentId,
        UUID authorId) implements EventPayload {

    public ExternalCommentAddedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        commentId = Objects.requireNonNull(commentId, "commentId must not be null");
        authorId = Objects.requireNonNull(authorId, "authorId must not be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
