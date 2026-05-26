package com.ticketmanagement.file.application;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.file.infrastructure.persistence.FileMetadataJpaRepository;

@Service
@RequiredArgsConstructor
public class FileMetadataQueryService {

    private final FileMetadataJpaRepository fileMetadataRepository;
    private final FileMetadataMapper fileMetadataMapper;

    // Metadata kaydini teknik kimligi ile getirir.
    @Transactional(readOnly = true)
    public FileMetadataResponse getMetadata(UUID id) {
        return fileMetadataRepository.findById(id)
                .map(fileMetadataMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("File metadata not found"));
    }
}
