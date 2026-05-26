package com.ticketmanagement.file.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.ticketmanagement.file.domain.FileUploadStatus;
import com.ticketmanagement.file.domain.FileValidationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "file_metadata", schema = "file_schema")
public class FileMetadataEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID ticketId;

    @Column(nullable = false, updatable = false)
    private UUID uploaderId;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, unique = true, length = 512)
    private String objectKey;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FileValidationStatus validationStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FileUploadStatus uploadStatus;

    @Column(nullable = false)
    private OffsetDateTime uploadExpiresAt;

    private OffsetDateTime completedAt;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public static FileMetadataEntity pending(
            UUID id,
            UUID ticketId,
            UUID uploaderId,
            String originalFilename,
            String objectKey,
            String contentType,
            long sizeBytes,
            OffsetDateTime now) {
        FileMetadataEntity metadata = new FileMetadataEntity();
        metadata.id = id;
        metadata.ticketId = ticketId;
        metadata.uploaderId = uploaderId;
        metadata.originalFilename = originalFilename;
        metadata.objectKey = objectKey;
        metadata.contentType = contentType;
        metadata.sizeBytes = sizeBytes;
        metadata.validationStatus = FileValidationStatus.PENDING;
        metadata.uploadStatus = FileUploadStatus.COMPLETED;
        metadata.uploadExpiresAt = now;
        metadata.completedAt = now;
        metadata.createdAt = now;
        metadata.updatedAt = now;
        return metadata;
    }

    public static FileMetadataEntity pendingUpload(
            UUID id,
            UUID ticketId,
            UUID uploaderId,
            String originalFilename,
            String objectKey,
            String contentType,
            long sizeBytes,
            OffsetDateTime uploadExpiresAt,
            OffsetDateTime now) {
        FileMetadataEntity metadata = new FileMetadataEntity();
        metadata.id = id;
        metadata.ticketId = ticketId;
        metadata.uploaderId = uploaderId;
        metadata.originalFilename = originalFilename;
        metadata.objectKey = objectKey;
        metadata.contentType = contentType;
        metadata.sizeBytes = sizeBytes;
        metadata.validationStatus = FileValidationStatus.PENDING;
        metadata.uploadStatus = FileUploadStatus.PENDING_UPLOAD;
        metadata.uploadExpiresAt = uploadExpiresAt;
        metadata.createdAt = now;
        metadata.updatedAt = now;
        return metadata;
    }

    public void completeUpload(OffsetDateTime now) {
        uploadStatus = FileUploadStatus.COMPLETED;
        completedAt = now;
        updatedAt = now;
    }
}
