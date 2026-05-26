package com.ticketmanagement.file.api.dto;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String errorCode,
        String message,
        String path) {

    public static ApiErrorResponse of(int status, String errorCode, String message, String path) {
        return new ApiErrorResponse(OffsetDateTime.now(), status, errorCode, message, path);
    }
}
