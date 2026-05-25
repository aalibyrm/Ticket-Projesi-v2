package com.ticketmanagement.ticket.application;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class TicketNumberGenerator {

    private final JdbcTemplate jdbcTemplate;

    String nextTicketNumber() {
        Long nextValue = jdbcTemplate.queryForObject("select nextval('ticket_schema.ticket_number_seq')", Long.class);
        if (nextValue == null) {
            throw new IllegalStateException("Could not generate ticket number");
        }
        return "TCK-%06d".formatted(nextValue);
    }
}

