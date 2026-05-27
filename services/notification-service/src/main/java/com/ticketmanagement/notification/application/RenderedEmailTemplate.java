package com.ticketmanagement.notification.application;

public record RenderedEmailTemplate(
        String subject,
        String textBody,
        String htmlBody) {

    public RenderedEmailTemplate {
        subject = requireText(subject, "subject");
        textBody = requireText(textBody, "textBody");
        htmlBody = requireText(htmlBody, "htmlBody");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
