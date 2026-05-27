package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketCommentJpaRepository extends JpaRepository<TicketCommentEntity, UUID> {
}
