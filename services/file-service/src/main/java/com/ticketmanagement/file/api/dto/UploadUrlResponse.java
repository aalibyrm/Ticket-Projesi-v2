package com.ticketmanagement.file.api.dto;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UploadUrlResponse(
        UUID fileId,
        String objectKey,
        String method,
        URI uploadUrl,
        Instant expiresAt,
        Map<String, String> requiredHeaders) {
}
