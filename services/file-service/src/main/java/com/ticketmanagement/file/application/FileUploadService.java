package com.ticketmanagement.file.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.ticketmanagement.file.api.dto.CreateUploadUrlRequest;
import com.ticketmanagement.file.api.dto.DownloadUrlResponse;
import com.ticketmanagement.file.api.dto.UploadUrlResponse;
import com.ticketmanagement.file.application.storage.ObjectStoragePort;
import com.ticketmanagement.file.application.storage.PresignedObjectOperation;
import com.ticketmanagement.file.application.ticket.TicketAccessContext;
import com.ticketmanagement.file.application.ticket.TicketAccessPort;
import com.ticketmanagement.file.domain.FileUploadStatus;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final ObjectProvider<ObjectStoragePort> objectStoragePortProvider;
    private final FileMetadataCommandService fileMetadataCommandService;
    private final FileMetadataQueryService fileMetadataQueryService;
    private final TicketAccessPort ticketAccessPort;

    // Dosya yukleme icin UUID tabanli object key ve presigned PUT URL uretir.
    public UploadUrlResponse createUploadUrl(TicketAccessContext context, CreateUploadUrlRequest request) {
        ticketAccessPort.assertCanAccessAttachment(request.ticketId(), context);
        String objectKey = "tickets/%s/%s".formatted(request.ticketId(), UUID.randomUUID());
        PresignedObjectOperation operation = objectStoragePort().createUploadUrl(objectKey, request.contentType().trim());
        FileMetadataResponse metadata = fileMetadataCommandService.reserveUpload(new ReserveFileUploadCommand(
                request.ticketId(),
                context.actorId(),
                request.originalFilename(),
                objectKey,
                request.contentType(),
                request.sizeBytes(),
                OffsetDateTime.ofInstant(operation.expiresAt(), ZoneOffset.UTC)));

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

    // Yetkili kullanici icin kisa sureli presigned GET URL uretir.
    public DownloadUrlResponse createDownloadUrl(UUID fileId, TicketAccessContext context) {
        FileMetadataResponse metadata = fileMetadataQueryService.getMetadata(fileId);
        ensureDownloadable(metadata);
        ticketAccessPort.assertCanAccessAttachment(metadata.ticketId(), context);

        PresignedObjectOperation operation = objectStoragePort().createDownloadUrl(metadata.objectKey());
        return new DownloadUrlResponse(
                metadata.id(),
                operation.method(),
                operation.url(),
                operation.expiresAt(),
                operation.requiredHeaders());
    }

    // Tamamlanmamis upload kayitlari icin download URL uretilmesini engeller.
    private void ensureDownloadable(FileMetadataResponse metadata) {
        if (metadata.uploadStatus() != FileUploadStatus.COMPLETED) {
            throw new FileNotReadyException();
        }
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
