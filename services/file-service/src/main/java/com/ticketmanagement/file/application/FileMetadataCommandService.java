package com.ticketmanagement.file.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.file.infrastructure.persistence.FileMetadataEntity;
import com.ticketmanagement.file.infrastructure.persistence.FileMetadataJpaRepository;

@Service
@RequiredArgsConstructor
public class FileMetadataCommandService {

    private final FileMetadataJpaRepository fileMetadataRepository;
    private final FileMetadataMapper fileMetadataMapper;
    private final Clock clock;

    // Yeni yukleme akisi icin PENDING durumunda dosya metadata kaydi olusturur.
    @Transactional
    public FileMetadataResponse createPendingMetadata(CreateFileMetadataCommand command) {
        validate(command);
        OffsetDateTime now = OffsetDateTime.now(clock);
        FileMetadataEntity metadata = FileMetadataEntity.pending(
                UUID.randomUUID(),
                command.ticketId(),
                command.uploaderId(),
                command.originalFilename().trim(),
                command.objectKey().trim(),
                command.contentType().trim(),
                command.sizeBytes(),
                now);

        return fileMetadataMapper.toResponse(fileMetadataRepository.save(metadata));
    }

    // Metadata olusturma komutunun zorunlu alanlarini kontrol eder.
    private static void validate(CreateFileMetadataCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.ticketId(), "ticketId is required");
        Objects.requireNonNull(command.uploaderId(), "uploaderId is required");
        if (isBlank(command.originalFilename())) {
            throw new IllegalArgumentException("originalFilename is required");
        }
        if (isBlank(command.objectKey())) {
            throw new IllegalArgumentException("objectKey is required");
        }
        if (isBlank(command.contentType())) {
            throw new IllegalArgumentException("contentType is required");
        }
        if (command.sizeBytes() < 1) {
            throw new IllegalArgumentException("sizeBytes must be positive");
        }
    }

    // String alanin bos veya null olup olmadigini kontrol eder.
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
