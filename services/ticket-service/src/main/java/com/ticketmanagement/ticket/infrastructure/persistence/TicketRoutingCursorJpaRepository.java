package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRoutingCursorJpaRepository extends JpaRepository<TicketRoutingCursorEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cursor from TicketRoutingCursorEntity cursor where cursor.topicId = :topicId")
    Optional<TicketRoutingCursorEntity> findByTopicIdForUpdate(@Param("topicId") UUID topicId);
}
