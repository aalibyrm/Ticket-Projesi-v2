package com.ticketmanagement.file.application.storage;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

public record PresignedObjectOperation(
        String method,
        URI url,
        Instant expiresAt,
        Map<String, String> requiredHeaders) {
}
