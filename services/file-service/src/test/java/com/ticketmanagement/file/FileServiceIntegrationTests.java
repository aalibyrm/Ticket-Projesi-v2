package com.ticketmanagement.file;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.file.application.CreateFileMetadataCommand;
import com.ticketmanagement.file.application.FileMetadataCommandService;
import com.ticketmanagement.file.application.FileMetadataQueryService;
import com.ticketmanagement.file.application.FileMetadataResponse;
import com.ticketmanagement.file.domain.FileValidationStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class FileServiceIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("file_app")
            .withPassword("file_dev_password")
            .withInitScript("testdb/init-file-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FileMetadataCommandService commandService;

    @Autowired
    private FileMetadataQueryService queryService;

    @Test
    void healthEndpointIsAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void createsAndReadsPendingFileMetadata() {
        UUID ticketId = UUID.randomUUID();
        UUID uploaderId = UUID.randomUUID();

        FileMetadataResponse created = commandService.createPendingMetadata(new CreateFileMetadataCommand(
                ticketId,
                uploaderId,
                "error-log.txt",
                "tickets/%s/error-log.txt".formatted(ticketId),
                "text/plain",
                4096));

        FileMetadataResponse stored = queryService.getMetadata(created.id());

        assertThat(stored.ticketId()).isEqualTo(ticketId);
        assertThat(stored.uploaderId()).isEqualTo(uploaderId);
        assertThat(stored.originalFilename()).isEqualTo("error-log.txt");
        assertThat(stored.objectKey()).isEqualTo("tickets/%s/error-log.txt".formatted(ticketId));
        assertThat(stored.contentType()).isEqualTo("text/plain");
        assertThat(stored.sizeBytes()).isEqualTo(4096);
        assertThat(stored.validationStatus()).isEqualTo(FileValidationStatus.PENDING);
        assertThat(stored.createdAt()).isNotNull();
        assertThat(stored.updatedAt()).isNotNull();
    }
}
