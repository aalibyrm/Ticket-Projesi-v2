package com.ticketmanagement.file.application;

public class FileNotReadyException extends RuntimeException {

    public FileNotReadyException() {
        super("File upload is not completed yet");
    }
}
