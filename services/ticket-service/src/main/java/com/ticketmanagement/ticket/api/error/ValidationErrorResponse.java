package com.ticketmanagement.ticket.api.error;

public record ValidationErrorResponse(String field, String message) {
}

