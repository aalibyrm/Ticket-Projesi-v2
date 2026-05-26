package com.ticketmanagement.file.application.ticket;

import java.util.UUID;

public interface TicketAccessPort {

    void assertCanAccessAttachment(UUID ticketId, TicketAccessContext context);
}
