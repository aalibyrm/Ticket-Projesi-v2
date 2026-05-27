package com.ticketmanagement.ticket.infrastructure.outbox;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
