package com.ticketmanagement.file.application;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.file.api.dto.AttachmentMetadataResponse;
import com.ticketmanagement.file.domain.FileUploadStatus;
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
                .orElseThrow(() -> NotFoundException.fileMetadata(id));
    }

    // Ticket detaylari icin tamamlanmis attachment metadata listesini getirir.
    @Transactional(readOnly = true)
    public List<AttachmentMetadataResponse> listCompletedAttachmentsForTicket(UUID ticketId) {
        return fileMetadataRepository
                .findByTicketIdAndUploadStatusOrderByCreatedAtAsc(ticketId, FileUploadStatus.COMPLETED)
                .stream()
                .map(fileMetadataMapper::toAttachmentMetadataResponse)
                .toList();
    }
}
