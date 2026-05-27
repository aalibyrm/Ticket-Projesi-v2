package com.ticketmanagement.ticket.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddExternalCommentRequest(
        @NotBlank
        @Size(max = 5000)
        String body) {
}
