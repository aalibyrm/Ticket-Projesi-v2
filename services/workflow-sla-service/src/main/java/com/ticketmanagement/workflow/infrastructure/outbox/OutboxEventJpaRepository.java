package com.ticketmanagement.workflow.infrastructure.outbox;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
            select *
            from workflow_schema.outbox_events
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
}
