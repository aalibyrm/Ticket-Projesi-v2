package com.ticketmanagement.file.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateUploadUrlRequest(
        @NotNull UUID ticketId,
        @NotBlank @Size(max = 255) String originalFilename,
        @NotBlank @Size(max = 120) String contentType,
        @Positive long sizeBytes) {
}
