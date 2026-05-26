package com.ticketmanagement.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.file.api.dto.ApiErrorResponse;
import com.ticketmanagement.file.api.dto.CreateUploadUrlRequest;
import com.ticketmanagement.file.api.dto.DownloadUrlResponse;
import com.ticketmanagement.file.api.dto.UploadUrlResponse;
import com.ticketmanagement.file.application.FileMetadataResponse;
import com.ticketmanagement.file.application.ForbiddenOperationException;
import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import com.ticketmanagement.file.application.storage.PresignedObjectOperation;
import com.ticketmanagement.file.application.ticket.TicketAccessContext;
import com.ticketmanagement.file.application.ticket.TicketAccessPort;
import com.ticketmanagement.file.domain.FileUploadStatus;
import com.ticketmanagement.file.domain.FileValidationStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class FileUploadApiIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("file_app")
            .withPassword("file_dev_password")
            .withInitScript("testdb/init-file-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private ObjectStoragePort objectStoragePort;

    @MockBean
    private TicketAccessPort ticketAccessPort;

    @Test
    void clientRequestsUploadUrlThenCompletesMetadata() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();
        stubLogPreviewWithKeyword();

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                UploadUrlResponse.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        UploadUrlResponse upload = uploadResponse.getBody();
        assertThat(upload).isNotNull();
        assertThat(upload.objectKey()).startsWith("tickets/%s/".formatted(ticketId));
        assertThat(upload.method()).isEqualTo("PUT");
        assertThat(upload.requiredHeaders()).containsEntry("Content-Type", "text/plain");
        assertThat(upload.uploadUrl().toString()).isEqualTo("https://r2.example/upload");
        assertThat(UUID.fromString(upload.objectKey().substring(upload.objectKey().lastIndexOf('/') + 1))).isNotNull();

        ResponseEntity<FileMetadataResponse> completeResponse = restTemplate.exchange(
                "/api/files/uploads/{id}/complete",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                FileMetadataResponse.class,
                upload.fileId());

        assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        FileMetadataResponse completed = completeResponse.getBody();
        assertThat(completed).isNotNull();
        assertThat(completed.ticketId()).isEqualTo(ticketId);
        assertThat(completed.uploaderId()).isEqualTo(actorId);
        assertThat(completed.objectKey()).isEqualTo(upload.objectKey());
        assertThat(completed.uploadStatus()).isEqualTo(FileUploadStatus.COMPLETED);
        assertThat(completed.validationStatus()).isEqualTo(FileValidationStatus.VALIDATED);
        assertThat(completed.completedAt()).isNotNull();
    }

    @Test
    void differentActorCannotCompleteReservedUpload() {
        UUID ownerId = UUID.randomUUID();
        UUID otherActorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(ownerId)),
                UploadUrlResponse.class);
        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ApiErrorResponse> completeResponse = restTemplate.exchange(
                "/api/files/uploads/{id}/complete",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(otherActorId)),
                ApiErrorResponse.class,
                uploadResponse.getBody().fileId());

        assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(completeResponse.getBody()).isNotNull();
        assertThat(completeResponse.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void clientRequestsDownloadUrlForCompletedMetadata() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();
        stubDownloadUrl();
        stubLogPreviewWithKeyword();

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                UploadUrlResponse.class);
        UploadUrlResponse upload = uploadResponse.getBody();
        assertThat(upload).isNotNull();

        restTemplate.exchange(
                "/api/files/uploads/{id}/complete",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                FileMetadataResponse.class,
                upload.fileId());

        ResponseEntity<DownloadUrlResponse> downloadResponse = restTemplate.exchange(
                "/api/files/{id}/download-url",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                DownloadUrlResponse.class,
                upload.fileId());

        assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        DownloadUrlResponse download = downloadResponse.getBody();
        assertThat(download).isNotNull();
        assertThat(download.fileId()).isEqualTo(upload.fileId());
        assertThat(download.method()).isEqualTo("GET");
        assertThat(download.downloadUrl().toString()).isEqualTo("https://r2.example/download");
        assertThat(download.requiredHeaders()).isEmpty();
        verify(objectStoragePort).createDownloadUrl(upload.objectKey());
    }

    @Test
    void authorizationFailurePreventsUploadUrlCreation() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        doThrow(ForbiddenOperationException.accessDenied())
                .when(ticketAccessPort)
                .assertCanAccessAttachment(eq(ticketId), any(TicketAccessContext.class));

        ResponseEntity<ApiErrorResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                ApiErrorResponse.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(uploadResponse.getBody()).isNotNull();
        assertThat(uploadResponse.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
        verify(objectStoragePort, never()).createUploadUrl(anyString(), anyString());
    }

    @Test
    void authorizationFailurePreventsDownloadUrlCreation() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();
        stubLogPreviewWithKeyword();

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                UploadUrlResponse.class);
        UploadUrlResponse upload = uploadResponse.getBody();
        assertThat(upload).isNotNull();

        restTemplate.exchange(
                "/api/files/uploads/{id}/complete",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                FileMetadataResponse.class,
                upload.fileId());

        reset(ticketAccessPort);
        doThrow(ForbiddenOperationException.accessDenied())
                .when(ticketAccessPort)
                .assertCanAccessAttachment(eq(ticketId), any(TicketAccessContext.class));

        ResponseEntity<ApiErrorResponse> downloadResponse = restTemplate.exchange(
                "/api/files/{id}/download-url",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                ApiErrorResponse.class,
                upload.fileId());

        assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(downloadResponse.getBody()).isNotNull();
        assertThat(downloadResponse.getBody().errorCode()).isEqualTo("ACCESS_DENIED");
        verify(objectStoragePort, never()).createDownloadUrl(anyString());
    }

    @Test
    void rejectsDisallowedFileExtensionBeforeUploadUrlCreation() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        ResponseEntity<ApiErrorResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(
                        new CreateUploadUrlRequest(ticketId, "payload.exe", "application/octet-stream", 4096),
                        actorHeaders(actorId)),
                ApiErrorResponse.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(uploadResponse.getBody()).isNotNull();
        assertThat(uploadResponse.getBody().errorCode()).isEqualTo("FILE_VALIDATION_FAILED");
        verify(ticketAccessPort, never()).assertCanAccessAttachment(any(UUID.class), any(TicketAccessContext.class));
        verify(objectStoragePort, never()).createUploadUrl(anyString(), anyString());
    }

    @Test
    void rejectsOversizedFileBeforeUploadUrlCreation() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        ResponseEntity<ApiErrorResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(
                        new CreateUploadUrlRequest(ticketId, "large.log", "text/plain", 10_485_761),
                        actorHeaders(actorId)),
                ApiErrorResponse.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(uploadResponse.getBody()).isNotNull();
        assertThat(uploadResponse.getBody().errorCode()).isEqualTo("FILE_VALIDATION_FAILED");
        verify(objectStoragePort, never()).createUploadUrl(anyString(), anyString());
    }

    @Test
    void rejectsMimeMismatchBeforeUploadUrlCreation() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();

        ResponseEntity<ApiErrorResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(
                        new CreateUploadUrlRequest(ticketId, "screenshot.png", "text/plain", 4096),
                        actorHeaders(actorId)),
                ApiErrorResponse.class);

        assertThat(uploadResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(uploadResponse.getBody()).isNotNull();
        assertThat(uploadResponse.getBody().errorCode()).isEqualTo("FILE_VALIDATION_FAILED");
        verify(objectStoragePort, never()).createUploadUrl(anyString(), anyString());
    }

    @Test
    void marksTextLogRejectedWhenRequiredKeywordIsMissing() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();
        when(objectStoragePort.readObjectPreview(anyString(), anyInt()))
                .thenReturn("normal startup completed without diagnostic terms");

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                UploadUrlResponse.class);
        UploadUrlResponse upload = uploadResponse.getBody();
        assertThat(upload).isNotNull();

        ResponseEntity<FileMetadataResponse> completeResponse = restTemplate.exchange(
                "/api/files/uploads/{id}/complete",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                FileMetadataResponse.class,
                upload.fileId());

        assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completeResponse.getBody()).isNotNull();
        assertThat(completeResponse.getBody().validationStatus()).isEqualTo(FileValidationStatus.REJECTED);
    }

    @Test
    void marksTextLogFailedWhenStoragePreviewCannotBeRead() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();
        when(objectStoragePort.readObjectPreview(anyString(), anyInt()))
                .thenThrow(new RuntimeException("preview unavailable"));

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                UploadUrlResponse.class);
        UploadUrlResponse upload = uploadResponse.getBody();
        assertThat(upload).isNotNull();

        ResponseEntity<FileMetadataResponse> completeResponse = restTemplate.exchange(
                "/api/files/uploads/{id}/complete",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                FileMetadataResponse.class,
                upload.fileId());

        assertThat(completeResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(completeResponse.getBody()).isNotNull();
        assertThat(completeResponse.getBody().validationStatus()).isEqualTo(FileValidationStatus.FAILED);
    }

    @Test
    void pendingUploadCannotReceiveDownloadUrl() {
        UUID actorId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        stubUploadUrl();

        ResponseEntity<UploadUrlResponse> uploadResponse = restTemplate.exchange(
                "/api/files/uploads",
                HttpMethod.POST,
                new HttpEntity<>(uploadRequest(ticketId), actorHeaders(actorId)),
                UploadUrlResponse.class);
        UploadUrlResponse upload = uploadResponse.getBody();
        assertThat(upload).isNotNull();

        ResponseEntity<ApiErrorResponse> downloadResponse = restTemplate.exchange(
                "/api/files/{id}/download-url",
                HttpMethod.POST,
                new HttpEntity<>(actorHeaders(actorId)),
                ApiErrorResponse.class,
                upload.fileId());

        assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(downloadResponse.getBody()).isNotNull();
        assertThat(downloadResponse.getBody().errorCode()).isEqualTo("FILE_NOT_READY");
        verify(objectStoragePort, never()).createDownloadUrl(anyString());
    }

    private void stubUploadUrl() {
        when(objectStoragePort.createUploadUrl(anyString(), eq("text/plain")))
                .thenReturn(new PresignedObjectOperation(
                        "PUT",
                        URI.create("https://r2.example/upload"),
                        Instant.parse("2030-01-01T00:10:00Z"),
                        Map.of("Content-Type", "text/plain")));
    }

    private void stubDownloadUrl() {
        when(objectStoragePort.createDownloadUrl(anyString()))
                .thenReturn(new PresignedObjectOperation(
                        "GET",
                        URI.create("https://r2.example/download"),
                        Instant.parse("2030-01-01T00:05:00Z"),
                        Map.of()));
    }

    private void stubLogPreviewWithKeyword() {
        when(objectStoragePort.readObjectPreview(anyString(), anyInt()))
                .thenReturn("Exception stacktrace captured during checkout flow");
    }

    private static CreateUploadUrlRequest uploadRequest(UUID ticketId) {
        return new CreateUploadUrlRequest(ticketId, "error-log.txt", "text/plain", 4096);
    }

    private static HttpHeaders actorHeaders(UUID actorId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Actor-Id", actorId.toString());
        return headers;
    }
}
