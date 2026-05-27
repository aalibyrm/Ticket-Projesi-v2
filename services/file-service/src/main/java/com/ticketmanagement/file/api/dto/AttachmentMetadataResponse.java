package com.ticketmanagement.file.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.file.domain.FileUploadStatus;
import com.ticketmanagement.file.domain.FileValidationStatus;

public record AttachmentMetadataResponse(
        UUID id,
        UUID ticketId,
        String originalFilename,
        String contentType,
        long sizeBytes,
        FileValidationStatus validationStatus,
        FileUploadStatus uploadStatus,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt) {
}
