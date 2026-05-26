package com.ticketmanagement.file.application;

public class StorageUnavailableException extends RuntimeException {

    public StorageUnavailableException() {
        super("Object storage is not configured");
    }
}
