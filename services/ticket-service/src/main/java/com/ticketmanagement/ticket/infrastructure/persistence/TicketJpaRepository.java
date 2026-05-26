package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
