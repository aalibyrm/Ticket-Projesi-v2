package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTopicJpaRepository extends JpaRepository<TicketTopicEntity, UUID> {

    List<TicketTopicEntity> findByActiveTrueOrderByNameAsc();
}
