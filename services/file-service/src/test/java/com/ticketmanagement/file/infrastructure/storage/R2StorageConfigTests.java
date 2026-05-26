package com.ticketmanagement.file.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import com.ticketmanagement.file.application.storage.PresignedObjectOperation;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class R2StorageConfigTests {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final String SECRET = "server-side-secret";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(R2StorageConfig.class)
            .withBean(Clock.class, () -> FIXED_CLOCK);

    @Test
    void doesNotCreateAdapterWhenR2IsDisabled() {
        contextRunner
                .withPropertyValues("app.storage.r2.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(ObjectStoragePort.class);
                    assertThat(context).doesNotHaveBean(S3Presigner.class);
                });
    }

    @Test
    void createsPresignedUploadAndDownloadOperationsWithoutExposingSecret() {
        contextRunner
                .withPropertyValues(
                        "app.storage.r2.enabled=true",
                        "app.storage.r2.endpoint=https://r2.example.com",
                        "app.storage.r2.region=auto",
                        "app.storage.r2.bucket=ticket-attachments-private",
                        "app.storage.r2.access-key-id=server-access-key",
                        "app.storage.r2.secret-access-key=" + SECRET,
                        "app.storage.r2.presign-upload-ttl=PT10M",
                        "app.storage.r2.presign-download-ttl=PT5M")
                .run(context -> {
                    ObjectStoragePort port = context.getBean(ObjectStoragePort.class);

                    PresignedObjectOperation upload = port.createUploadUrl("tickets/1/error-log.txt", "text/plain");
                    PresignedObjectOperation download = port.createDownloadUrl("tickets/1/error-log.txt");

                    assertThat(upload.method()).isEqualTo("PUT");
                    assertThat(upload.expiresAt()).isEqualTo(NOW.plusSeconds(600));
                    assertThat(upload.requiredHeaders()).containsEntry("Content-Type", "text/plain");
                    assertThat(upload.url().toString())
                            .contains("X-Amz-Signature")
                            .contains("ticket-attachments-private")
                            .doesNotContain(SECRET);

                    assertThat(download.method()).isEqualTo("GET");
                    assertThat(download.expiresAt()).isEqualTo(NOW.plusSeconds(300));
                    assertThat(download.requiredHeaders()).isEmpty();
                    assertThat(download.url().toString())
                            .contains("X-Amz-Signature")
                            .contains("ticket-attachments-private")
                            .doesNotContain(SECRET);
                });
    }

    @Test
    void failsFastWhenEnabledConfigIsMissing() {
        contextRunner
                .withPropertyValues("app.storage.r2.enabled=true")
                .run(context -> assertThat(context)
                        .hasFailed()
                        .getFailure()
                        .hasMessageContaining("R2 endpoint must be configured"));
    }
}
