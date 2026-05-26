package com.ticketmanagement.file.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReserveFileUploadCommand(
        UUID ticketId,
        UUID uploaderId,
        String originalFilename,
        String objectKey,
        String contentType,
        long sizeBytes,
        OffsetDateTime uploadExpiresAt) {
}
