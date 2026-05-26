package com.ticketmanagement.file.application;

public class TicketAccessUnavailableException extends RuntimeException {

    public TicketAccessUnavailableException() {
        super("Ticket access service is unavailable");
    }
}
