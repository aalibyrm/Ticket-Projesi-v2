package com.ticketmanagement.file.api;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticketmanagement.file.api.dto.AttachmentMetadataResponse;
import com.ticketmanagement.file.application.FileMetadataQueryService;

@RestController
@RequestMapping("/internal/tickets")
@RequiredArgsConstructor
class InternalAttachmentMetadataController {

    private final FileMetadataQueryService fileMetadataQueryService;

    // Ticket-service icin tamamlanmis attachment metadata listesini dondurur.
    @GetMapping("/{ticketId}/attachments")
    List<AttachmentMetadataResponse> listTicketAttachments(@PathVariable UUID ticketId) {
        return fileMetadataQueryService.listCompletedAttachmentsForTicket(ticketId);
    }
}
