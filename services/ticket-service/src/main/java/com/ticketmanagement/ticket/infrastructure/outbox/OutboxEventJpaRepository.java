package com.ticketmanagement.ticket.infrastructure.outbox;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    List<OutboxEventEntity> findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    @Query(value = """
            select *
            from ticket_schema.outbox_events
            where status = 'PENDING'
               or (
                    status in ('FAILED', 'PROCESSING')
                    and retry_count < :maxRetries
                    and next_attempt_at <= now()
                  )
            order by created_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<OutboxEventEntity> findClaimableForUpdate(
            @Param("maxRetries") int maxRetries,
            @Param("batchSize") int batchSize);

    @Query(value = """
            select max(occurred_at)
            from ticket_schema.outbox_events
            where aggregate_id = :ticketId
              and event_type = 'ticket.status-changed'
              and payload ->> 'newStatus' = :status
            """, nativeQuery = true)
    Instant findLatestTicketStatusChangedTo(
            @Param("ticketId") UUID ticketId,
            @Param("status") String status);
}
