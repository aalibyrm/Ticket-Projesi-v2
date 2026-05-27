package com.ticketmanagement.ticket.application;

public class InvalidTicketOperationException extends RuntimeException {

    private InvalidTicketOperationException(String message) {
        super(message);
    }

    public static InvalidTicketOperationException statusMustChange() {
        return new InvalidTicketOperationException("Ticket status must change");
    }
}
