package com.ticketmanagement.ticket.api.error;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        String correlationId,
        List<ValidationErrorResponse> validationErrors) {

    public static ApiErrorResponse of(
            int status,
            String errorCode,
            String message,
            String path,
            String correlationId,
            List<ValidationErrorResponse> validationErrors) {
        return new ApiErrorResponse(
                OffsetDateTime.now(),
                status,
                errorCode,
                message,
                path,
                correlationId,
                validationErrors == null ? List.of() : validationErrors);
    }
}

