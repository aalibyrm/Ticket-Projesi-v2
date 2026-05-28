package com.ticketmanagement.reporting.application;

import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionEntity;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionJpaRepository;
import com.ticketmanagement.reporting.infrastructure.persistence.AgentWorklogProjectionEntity;
import com.ticketmanagement.reporting.infrastructure.persistence.AgentWorklogProjectionJpaRepository;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

@Service
@RequiredArgsConstructor
public class ReportingProjectionService {

    private final TicketReportProjectionJpaRepository ticketReportProjectionRepository;
    private final AgentWorklogProjectionJpaRepository agentWorklogProjectionRepository;

    // Ticket rapor projection kaydini event snapshot bilgisiyle olusturur veya gunceller.
    @Transactional
    public TicketReportProjectionEntity upsertTicketProjection(TicketProjectionUpsertCommand command) {
        return ticketReportProjectionRepository.findById(command.ticketId())
                .map(existingProjection -> {
                    existingProjection.apply(command);
                    return existingProjection;
                })
                .orElseGet(() -> ticketReportProjectionRepository.save(TicketReportProjectionEntity.from(command)));
    }

    // Raporlama sorgulari icin ticket projection kaydini kimligine gore getirir.
    @Transactional(readOnly = true)
    public Optional<TicketReportProjectionEntity> findTicketProjection(UUID ticketId) {
        return ticketReportProjectionRepository.findById(ticketId);
    }

    // Ticket assignment eventini projection snapshot kaydina uygular.
    @Transactional
    public boolean updateTicketAssignment(
            UUID ticketId,
            UUID assigneeId,
            UUID assignedTeamId,
            OffsetDateTime updatedAt) {
        return ticketReportProjectionRepository.findById(ticketId)
                .map(projection -> {
                    projection.updateAssignment(assigneeId, assignedTeamId, updatedAt);
                    return true;
                })
                .orElse(false);
    }

    // Ticket status eventini projection snapshot kaydina uygular.
    @Transactional
    public boolean updateTicketStatus(UUID ticketId, ProjectionTicketStatus status, OffsetDateTime updatedAt) {
        return ticketReportProjectionRepository.findById(ticketId)
                .map(projection -> {
                    projection.updateStatus(status, updatedAt);
                    return true;
                })
                .orElse(false);
    }

    // Workflow SLA eventini projection snapshot kaydina uygular.
    @Transactional
    public boolean updateTicketSlaStatus(
            UUID ticketId,
            ProjectionSlaStatus slaStatus,
            OffsetDateTime targetResolutionAt,
            OffsetDateTime updatedAt) {
        return ticketReportProjectionRepository.findById(ticketId)
                .map(projection -> {
                    projection.updateSla(slaStatus, targetResolutionAt, updatedAt);
                    return true;
                })
                .orElse(false);
    }

    // Agent worklog projection kaydini worklogId bazinda idempotent olarak olusturur veya gunceller.
    @Transactional
    public AgentWorklogProjectionEntity upsertAgentWorklogProjection(AgentWorklogProjectionCommand command) {
        return agentWorklogProjectionRepository.findById(command.worklogId())
                .map(existingProjection -> {
                    existingProjection.apply(command);
                    return existingProjection;
                })
                .orElseGet(() -> agentWorklogProjectionRepository.save(AgentWorklogProjectionEntity.from(command)));
    }
}
