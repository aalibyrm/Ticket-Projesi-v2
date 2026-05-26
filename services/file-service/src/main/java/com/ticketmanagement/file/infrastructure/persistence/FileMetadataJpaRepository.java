package com.ticketmanagement.file.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataEntity, UUID> {

    Optional<FileMetadataEntity> findByObjectKey(String objectKey);
}
