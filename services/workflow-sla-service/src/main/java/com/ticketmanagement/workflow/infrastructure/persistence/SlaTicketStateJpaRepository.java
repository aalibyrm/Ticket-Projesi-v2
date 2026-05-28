package com.ticketmanagement.workflow.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SlaTicketStateJpaRepository extends JpaRepository<SlaTicketStateEntity, UUID> {

    @Query(value = """
            select *
            from workflow_schema.sla_ticket_states
            where status in ('ACTIVE', 'AT_RISK')
              and customer_id is not null
              and target_resolution_at <= :detectedAt
            order by target_resolution_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<SlaTicketStateEntity> findBreachCandidatesForUpdate(
            @Param("detectedAt") OffsetDateTime detectedAt,
            @Param("batchSize") int batchSize);

    @Query(value = """
            select *
            from workflow_schema.sla_ticket_states
            where status = 'ACTIVE'
              and customer_id is not null
              and target_resolution_at > :detectedAt
              and (
                    (priority = 'HIGH' and target_resolution_at <= :highRiskThreshold)
                 or (priority = 'MEDIUM' and target_resolution_at <= :mediumRiskThreshold)
                 or (priority = 'LOW' and target_resolution_at <= :lowRiskThreshold)
              )
            order by target_resolution_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<SlaTicketStateEntity> findRiskCandidatesForUpdate(
            @Param("detectedAt") OffsetDateTime detectedAt,
            @Param("highRiskThreshold") OffsetDateTime highRiskThreshold,
            @Param("mediumRiskThreshold") OffsetDateTime mediumRiskThreshold,
            @Param("lowRiskThreshold") OffsetDateTime lowRiskThreshold,
            @Param("batchSize") int batchSize);
}
