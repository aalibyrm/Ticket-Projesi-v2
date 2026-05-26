package com.ticketmanagement.file.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.file.domain.FileUploadStatus;
import com.ticketmanagement.file.domain.FileValidationStatus;

public record FileMetadataResponse(
        UUID id,
        UUID ticketId,
        UUID uploaderId,
        String originalFilename,
        String objectKey,
        String contentType,
        long sizeBytes,
        FileValidationStatus validationStatus,
        FileUploadStatus uploadStatus,
        OffsetDateTime uploadExpiresAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
}
