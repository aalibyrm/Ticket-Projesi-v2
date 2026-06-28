package com.ticketmanagement.ticket.infrastructure.file;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;
import com.ticketmanagement.ticket.application.AttachmentLookupContext;
import com.ticketmanagement.ticket.application.TicketAttachmentPort;

@Component
@RequiredArgsConstructor
class FileServiceTicketAttachmentAdapter implements TicketAttachmentPort {

    private static final ParameterizedTypeReference<List<TicketAttachmentResponse>> ATTACHMENT_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    @Qualifier("fileServiceRestClient")
    private final RestClient fileServiceRestClient;

    @Override
    public List<TicketAttachmentResponse> listAttachments(UUID ticketId, AttachmentLookupContext context) {
        try {
            List<TicketAttachmentResponse> attachments = fileServiceRestClient.get()
                    .uri("/internal/tickets/{ticketId}/attachments", ticketId)
                    .headers(headers -> applyAuthorization(headers, context))
                    .retrieve()
                    .body(ATTACHMENT_LIST_TYPE);
            return attachments == null ? List.of() : attachments;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Attachment metadata service unavailable",
                    exception);
        }
    }

    private void applyAuthorization(HttpHeaders headers, AttachmentLookupContext context) {
        if (context != null && context.bearerToken() != null && !context.bearerToken().isBlank()) {
            headers.setBearerAuth(context.bearerToken());
        }
    }
}
