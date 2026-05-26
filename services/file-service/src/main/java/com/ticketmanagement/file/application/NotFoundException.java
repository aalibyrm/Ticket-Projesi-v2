package com.ticketmanagement.file.application;

import java.util.UUID;

public class NotFoundException extends RuntimeException {

    private NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException fileMetadata(UUID id) {
        return new NotFoundException("File metadata not found: " + id);
    }
}
