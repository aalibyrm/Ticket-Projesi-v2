package com.ticketmanagement.reporting.api.dto;

import java.time.LocalDate;

public record ClosedTicketDailyCountResponse(
        LocalDate date,
        long count) {
}
