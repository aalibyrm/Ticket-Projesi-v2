package com.ticketmanagement.file.application;

public class ForbiddenOperationException extends RuntimeException {

    private ForbiddenOperationException(String message) {
        super(message);
    }

    public static ForbiddenOperationException accessDenied() {
        return new ForbiddenOperationException("Access denied");
    }
}
