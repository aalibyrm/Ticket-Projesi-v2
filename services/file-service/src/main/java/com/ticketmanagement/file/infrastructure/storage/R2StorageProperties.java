package com.ticketmanagement.file.infrastructure.storage;

import java.net.URI;
import java.time.Duration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage.r2")
class R2StorageProperties {

    private boolean enabled;
    private URI endpoint;
    private String region = "auto";
    private String bucket;
    private String accessKeyId;
    private String secretAccessKey;
    private Duration presignUploadTtl = Duration.ofMinutes(10);
    private Duration presignDownloadTtl = Duration.ofMinutes(5);

    void validateEnabledConfig() {
        if (endpoint == null) {
            throw new IllegalStateException("R2 endpoint must be configured");
        }
        requireText(region, "R2 region must be configured");
        requireText(bucket, "R2 bucket must be configured");
        requireText(accessKeyId, "R2 access key id must be configured");
        requireText(secretAccessKey, "R2 secret access key must be configured");
        requirePositive(presignUploadTtl, "R2 upload presign ttl must be positive");
        requirePositive(presignDownloadTtl, "R2 download presign ttl must be positive");
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(message);
        }
    }

    private static void requirePositive(Duration value, String message) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalStateException(message);
        }
    }
}
