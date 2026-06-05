package com.ticketmanagement.ticket.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ticketmanagement.ticket.domain.TicketConversationReadScope;

public interface TicketConversationReadJpaRepository extends JpaRepository<TicketConversationReadEntity, UUID> {

    Optional<TicketConversationReadEntity> findByTicketIdAndActorIdAndScope(
            UUID ticketId,
            UUID actorId,
            TicketConversationReadScope scope);
}
