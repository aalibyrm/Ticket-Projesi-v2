package com.ticketmanagement.file.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.file.api.dto.ApiErrorResponse;
import com.ticketmanagement.file.application.FileNotReadyException;
import com.ticketmanagement.file.application.FileValidationException;
import com.ticketmanagement.file.application.ForbiddenOperationException;
import com.ticketmanagement.file.application.NotFoundException;
import com.ticketmanagement.file.application.StorageUnavailableException;
import com.ticketmanagement.file.application.TicketAccessUnavailableException;
import com.ticketmanagement.file.application.UploadExpiredException;

@RestControllerAdvice
class FileApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", request);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    ResponseEntity<ApiErrorResponse> handleForbiddenException(
            ForbiddenOperationException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", exception.getMessage(), request);
    }

    @ExceptionHandler(UploadExpiredException.class)
    ResponseEntity<ApiErrorResponse> handleUploadExpiredException(
            UploadExpiredException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.GONE, "UPLOAD_EXPIRED", exception.getMessage(), request);
    }

    @ExceptionHandler(FileNotReadyException.class)
    ResponseEntity<ApiErrorResponse> handleFileNotReadyException(
            FileNotReadyException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "FILE_NOT_READY", exception.getMessage(), request);
    }

    @ExceptionHandler(FileValidationException.class)
    ResponseEntity<ApiErrorResponse> handleFileValidationException(
            FileValidationException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "FILE_VALIDATION_FAILED", exception.getMessage(), request);
    }

    @ExceptionHandler(StorageUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleStorageUnavailableException(
            StorageUnavailableException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "STORAGE_UNAVAILABLE", exception.getMessage(), request);
    }

    @ExceptionHandler(TicketAccessUnavailableException.class)
    ResponseEntity<ApiErrorResponse> handleTicketAccessUnavailableException(
            TicketAccessUnavailableException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "TICKET_ACCESS_UNAVAILABLE",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return buildResponse(status, errorCodeFor(status), messageFor(exception, status), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), request);
    }

    private static ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(status.value(), errorCode, message, request.getRequestURI());
        return ResponseEntity.status(status).body(response);
    }

    private static String errorCodeFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "INVALID_REQUEST";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "ACCESS_DENIED";
            default -> "REQUEST_FAILED";
        };
    }

    private static String messageFor(ResponseStatusException exception, HttpStatus status) {
        String reason = exception.getReason();
        if (reason == null || reason.isBlank()) {
            return status.getReasonPhrase();
        }
        return reason;
    }
}
