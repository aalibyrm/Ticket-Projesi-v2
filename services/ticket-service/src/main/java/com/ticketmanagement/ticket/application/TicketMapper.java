package com.ticketmanagement.ticket.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;

@Component
class TicketMapper {

    TicketResponse toResponse(TicketEntity ticket) {
        return toResponse(ticket, List.of());
    }

    TicketResponse toResponse(TicketEntity ticket, List<TicketAttachmentResponse> attachments) {
        ProductEntity product = ticket.getProduct();
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getCustomerId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                ticket.getSummary(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                attachments,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}
