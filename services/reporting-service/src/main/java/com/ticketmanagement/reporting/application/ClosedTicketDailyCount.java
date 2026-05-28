package com.ticketmanagement.reporting.application;

import java.time.LocalDate;
import java.util.Objects;

public record ClosedTicketDailyCount(
        LocalDate date,
        long count) {

    public ClosedTicketDailyCount {
        date = Objects.requireNonNull(date, "date must not be null");
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }
}
