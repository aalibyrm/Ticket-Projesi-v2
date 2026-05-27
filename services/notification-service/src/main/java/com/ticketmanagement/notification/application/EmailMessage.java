package com.ticketmanagement.notification.application;

public record EmailMessage(
        String recipientEmail,
        String subject,
        String textBody,
        String htmlBody) {

    public EmailMessage {
        recipientEmail = requireText(recipientEmail, "recipientEmail");
        subject = requireText(subject, "subject");
        textBody = requireText(textBody, "textBody");
        htmlBody = htmlBody == null || htmlBody.isBlank() ? null : htmlBody;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
