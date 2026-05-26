package com.ticketmanagement.file.application;

import java.util.UUID;

public record CreateFileMetadataCommand(
        UUID ticketId,
        UUID uploaderId,
        String originalFilename,
        String objectKey,
        String contentType,
        long sizeBytes) {
}
