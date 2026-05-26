package com.ticketmanagement.file.application;

public class UploadExpiredException extends RuntimeException {

    public UploadExpiredException() {
        super("Upload URL has expired");
    }
}
