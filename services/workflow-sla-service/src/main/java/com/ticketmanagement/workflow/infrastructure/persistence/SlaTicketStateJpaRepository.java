package com.ticketmanagement.workflow.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaTicketStateJpaRepository extends JpaRepository<SlaTicketStateEntity, UUID> {
}
