package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {

    List<TicketEntity> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ticket from TicketEntity ticket where ticket.id = :id")
    Optional<TicketEntity> findByIdForUpdate(@Param("id") UUID id);
}
