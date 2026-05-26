package com.ticketmanagement.file.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.ticketmanagement.file.api.dto.CreateUploadUrlRequest;
import com.ticketmanagement.file.api.dto.UploadUrlResponse;
import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import com.ticketmanagement.file.application.storage.PresignedObjectOperation;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final ObjectProvider<ObjectStoragePort> objectStoragePortProvider;
    private final FileMetadataCommandService fileMetadataCommandService;

    // Dosya yukleme icin UUID tabanli object key ve presigned PUT URL uretir.
    public UploadUrlResponse createUploadUrl(UUID actorId, CreateUploadUrlRequest request) {
        String objectKey = "tickets/%s/%s".formatted(request.ticketId(), UUID.randomUUID());
        PresignedObjectOperation operation = objectStoragePort().createUploadUrl(objectKey, request.contentType().trim());
        FileMetadataResponse metadata = fileMetadataCommandService.reserveUpload(new ReserveFileUploadCommand(
                request.ticketId(),
                actorId,
                request.originalFilename(),
                objectKey,
                request.contentType(),
                request.sizeBytes(),
                OffsetDateTime.ofInstant(operation.expiresAt(), java.time.ZoneOffset.UTC)));

        return new UploadUrlResponse(
                metadata.id(),
                metadata.objectKey(),
                operation.method(),
                operation.url(),
                operation.expiresAt(),
                operation.requiredHeaders());
    }

    // Upload tamamlandiktan sonra metadata kaydini actor sahipligiyle tamamlar.
    public FileMetadataResponse completeUpload(UUID actorId, UUID fileId) {
        return fileMetadataCommandService.completeUpload(fileId, actorId);
    }

    // Konfigure edilmis object storage adapterini getirir.
    private ObjectStoragePort objectStoragePort() {
        ObjectStoragePort objectStoragePort = objectStoragePortProvider.getIfAvailable();
        if (objectStoragePort == null) {
            throw new StorageUnavailableException();
        }
        return objectStoragePort;
    }
}
