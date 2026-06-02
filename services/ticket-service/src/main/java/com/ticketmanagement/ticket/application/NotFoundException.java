package com.ticketmanagement.ticket.application;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    private NotFoundException(String message) {
        super(message);
    }

    static NotFoundException product(UUID productId) {
        return new NotFoundException("Product not found: " + productId);
    }

    static NotFoundException ticket(UUID ticketId) {
        return new NotFoundException("Ticket not found: " + ticketId);
    }

    static NotFoundException team(UUID teamId) {
        return new NotFoundException("Team not found: " + teamId);
    }
}
