package com.ticketmanagement.file.api.dto;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DownloadUrlResponse(
        UUID fileId,
        String method,
        URI downloadUrl,
        Instant expiresAt,
        Map<String, String> requiredHeaders) {
}
