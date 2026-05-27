package com.ticketmanagement.notification.domain;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailTemplateKey {
    TICKET_CREATED("ticket-created"),
    TICKET_STATUS_CHANGED("ticket-status-changed"),
    TICKET_ASSIGNED("ticket-assigned"),
    TICKET_EXTERNAL_COMMENT_ADDED("ticket-external-comment-added"),
    SLA_RISK("sla-risk"),
    SLA_BREACH("sla-breach"),
    TICKET_RESOLVED("ticket-resolved"),
    TICKET_CLOSED("ticket-closed");

    private final String value;

    public static EmailTemplateKey fromValue(String value) {
        return Arrays.stream(values())
                .filter(templateKey -> templateKey.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported email template key: " + value));
    }
}
