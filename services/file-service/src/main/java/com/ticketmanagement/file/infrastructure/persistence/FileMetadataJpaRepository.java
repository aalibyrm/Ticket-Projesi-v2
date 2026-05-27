package com.ticketmanagement.file.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketmanagement.file.domain.FileUploadStatus;

public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataEntity, UUID> {

    Optional<FileMetadataEntity> findByObjectKey(String objectKey);

    List<FileMetadataEntity> findByTicketIdAndUploadStatusOrderByCreatedAtAsc(
            UUID ticketId,
            FileUploadStatus uploadStatus);
}
