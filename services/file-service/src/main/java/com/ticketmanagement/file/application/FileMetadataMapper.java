package com.ticketmanagement.file.application;

import org.springframework.stereotype.Component;

import com.ticketmanagement.file.api.dto.AttachmentMetadataResponse;
import com.ticketmanagement.file.infrastructure.persistence.FileMetadataEntity;

@Component
class FileMetadataMapper {

    FileMetadataResponse toResponse(FileMetadataEntity metadata) {
        return new FileMetadataResponse(
                metadata.getId(),
                metadata.getTicketId(),
                metadata.getUploaderId(),
                metadata.getOriginalFilename(),
                metadata.getObjectKey(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                metadata.getValidationStatus(),
                metadata.getUploadStatus(),
                metadata.getUploadExpiresAt(),
                metadata.getCompletedAt(),
                metadata.getCreatedAt(),
                metadata.getUpdatedAt());
    }

    AttachmentMetadataResponse toAttachmentMetadataResponse(FileMetadataEntity metadata) {
        return new AttachmentMetadataResponse(
                metadata.getId(),
                metadata.getTicketId(),
                metadata.getOriginalFilename(),
                metadata.getContentType(),
                metadata.getSizeBytes(),
                metadata.getValidationStatus(),
                metadata.getUploadStatus(),
                metadata.getCompletedAt(),
                metadata.getCreatedAt());
    }
}
