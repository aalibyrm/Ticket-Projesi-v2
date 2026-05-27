package com.ticketmanagement.ticket.api.error;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.ticket.application.ForbiddenOperationException;
import com.ticketmanagement.ticket.application.InvalidTicketOperationException;
import com.ticketmanagement.ticket.application.NotFoundException;
import com.ticketmanagement.ticket.infrastructure.web.CorrelationIdFilter;

@Log4j2
@RestControllerAdvice
class GlobalExceptionHandler {

    // Request validation hatalarini standart 400 response formatina cevirir.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ValidationErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorResponse(error.getField(), error.getDefaultMessage()))
                .toList();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Request validation failed",
                request,
                errors);
    }

    // Hatali path/header parametre tiplerini standart 400 response formatina cevirir.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_PARAMETER",
                "Request parameter has invalid format",
                request,
                List.of(new ValidationErrorResponse(exception.getName(), "Invalid format")));
    }

    // Bulunamayan kaynaklari hassas detay sizdirmeden standart 404 response formatina cevirir.
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiErrorResponse> handleNotFoundException(NotFoundException exception, HttpServletRequest request) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request,
                List.of());
    }

    // Yetkisiz is kurali ihlallerini standart 403 response formatina cevirir.
    @ExceptionHandler(ForbiddenOperationException.class)
    ResponseEntity<ApiErrorResponse> handleForbiddenOperationException(
            ForbiddenOperationException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                exception.getMessage(),
                request,
                List.of());
    }

    // Gecersiz ticket is aksiyonlarini standart 400 response formatina cevirir.
    @ExceptionHandler(InvalidTicketOperationException.class)
    ResponseEntity<ApiErrorResponse> handleInvalidTicketOperationException(
            InvalidTicketOperationException exception,
            HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_TICKET_OPERATION",
                exception.getMessage(),
                request,
                List.of());
    }

    // Controller tarafindan verilen HTTP status hatalarini standart response formatina cevirir.
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return buildResponse(
                status,
                errorCodeFor(status),
                messageFor(exception, status),
                request,
                List.of());
    }

    // Beklenmeyen hatalari loglar ve client'a guvenli 500 response dondurur.
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception while processing request", exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unexpected server error",
                request,
                List.of());
    }

    private static ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            List<ValidationErrorResponse> validationErrors) {
        ApiErrorResponse response = ApiErrorResponse.of(
                status.value(),
                errorCode,
                message,
                request.getRequestURI(),
                CorrelationIdFilter.currentCorrelationId(),
                validationErrors);
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
