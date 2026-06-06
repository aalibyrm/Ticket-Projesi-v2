package com.ticketmanagement.event.ticket;

import java.util.Objects;
import java.util.UUID;

import com.ticketmanagement.event.EventPayload;

public record TicketCreatedPayload(
        UUID ticketId,
        String ticketNumber,
        UUID customerId,
        UUID productId,
        String topicCode,
        String topicName,
        UUID routedDepartmentId,
        String routedDepartmentCode,
        String routedDepartmentName,
        UUID assignedTeamId,
        String assignedTeamCode,
        String assignedTeamName,
        UUID routedSupportActorId,
        String priority,
        String status) implements EventPayload {

    public TicketCreatedPayload(
            UUID ticketId,
            String ticketNumber,
            UUID customerId,
            UUID productId,
            String priority,
            String status) {
        this(
                ticketId,
                ticketNumber,
                customerId,
                productId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                priority,
                status);
    }

    public TicketCreatedPayload(
            UUID ticketId,
            String ticketNumber,
            UUID customerId,
            UUID productId,
            String topicCode,
            String topicName,
            UUID routedDepartmentId,
            String routedDepartmentCode,
            String routedDepartmentName,
            String priority,
            String status) {
        this(
                ticketId,
                ticketNumber,
                customerId,
                productId,
                topicCode,
                topicName,
                routedDepartmentId,
                routedDepartmentCode,
                routedDepartmentName,
                null,
                null,
                null,
                null,
                priority,
                status);
    }

    public TicketCreatedPayload {
        ticketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        ticketNumber = requireText(ticketNumber, "ticketNumber");
        customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        productId = Objects.requireNonNull(productId, "productId must not be null");
        topicCode = optionalText(topicCode);
        topicName = optionalText(topicName);
        routedDepartmentCode = optionalText(routedDepartmentCode);
        routedDepartmentName = optionalText(routedDepartmentName);
        assignedTeamCode = optionalText(assignedTeamCode);
        assignedTeamName = optionalText(assignedTeamName);
        priority = requireText(priority, "priority");
        status = requireText(status, "status");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String optionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
