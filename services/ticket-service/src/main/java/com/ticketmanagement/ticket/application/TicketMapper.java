package com.ticketmanagement.ticket.application;

import org.springframework.stereotype.Component;

import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;

@Component
class TicketMapper {

    TicketResponse toResponse(TicketEntity ticket) {
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
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}

