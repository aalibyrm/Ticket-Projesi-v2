package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketWorklogJpaRepository extends JpaRepository<TicketWorklogEntity, UUID> {

    List<TicketWorklogEntity> findByTicketIdOrderByCreatedAtDesc(UUID ticketId);
}
