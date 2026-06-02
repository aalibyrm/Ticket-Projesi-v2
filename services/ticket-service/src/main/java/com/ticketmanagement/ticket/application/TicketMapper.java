package com.ticketmanagement.ticket.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ticketmanagement.ticket.api.dto.TicketAttachmentResponse;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.infrastructure.persistence.DepartmentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketCommentEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketTopicEntity;
import com.ticketmanagement.ticket.infrastructure.persistence.TicketWorklogEntity;

@Component
class TicketMapper {

    TicketResponse toResponse(TicketEntity ticket) {
        return toResponse(ticket, List.of());
    }

    TicketResponse toResponse(TicketEntity ticket, List<TicketAttachmentResponse> attachments) {
        ProductEntity product = ticket.getProduct();
        TicketTopicEntity topic = ticket.getTopic();
        DepartmentEntity department = ticket.getRoutedDepartment();
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getCustomerId(),
                product.getId(),
                product.getCode(),
                product.getName(),
                topic == null ? null : topic.getCode(),
                topic == null ? null : topic.getName(),
                department == null ? null : department.getId(),
                department == null ? null : department.getCode(),
                department == null ? null : department.getName(),
                ticket.getSummary(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssigneeId(),
                ticket.getAssignedTeamId(),
                attachments,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    TicketCommentResponse toResponse(TicketCommentEntity comment) {
        return new TicketCommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthorId(),
                comment.getVisibility(),
                comment.getBody(),
                comment.getCreatedAt());
    }

    TicketWorklogResponse toResponse(TicketWorklogEntity worklog) {
        return new TicketWorklogResponse(
                worklog.getId(),
                worklog.getTicket().getId(),
                worklog.getAgentId(),
                worklog.getWorkDate(),
                worklog.getDurationMinutes(),
                worklog.getDescription(),
                worklog.getCreatedAt());
    }
}
