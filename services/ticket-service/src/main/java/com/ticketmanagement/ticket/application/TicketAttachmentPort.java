package com.ticketmanagement.ticket.application;

import java.util.List;
import java.util.UUID;

import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;

public interface TicketAttachmentPort {

    List<TicketAttachmentResponse> listAttachments(UUID ticketId, AttachmentLookupContext context);
}
