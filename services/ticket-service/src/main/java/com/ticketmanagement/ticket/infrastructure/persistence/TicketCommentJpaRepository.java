package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketmanagement.ticket.domain.TicketCommentVisibility;

public interface TicketCommentJpaRepository extends JpaRepository<TicketCommentEntity, UUID> {

    List<TicketCommentEntity> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);

    List<TicketCommentEntity> findByTicketIdAndVisibilityOrderByCreatedAtAsc(
            UUID ticketId,
            TicketCommentVisibility visibility);
}
