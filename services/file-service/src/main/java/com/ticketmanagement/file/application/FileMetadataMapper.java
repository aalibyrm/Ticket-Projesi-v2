package com.ticketmanagement.file.application;

import org.springframework.stereotype.Component;

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
                metadata.getCreatedAt(),
                metadata.getUpdatedAt());
    }
}
