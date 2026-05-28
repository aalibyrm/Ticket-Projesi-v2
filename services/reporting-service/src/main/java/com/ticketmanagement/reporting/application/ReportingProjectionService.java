package com.ticketmanagement.reporting.application;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionEntity;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionJpaRepository;

@Service
@RequiredArgsConstructor
public class ReportingProjectionService {

    private final TicketReportProjectionJpaRepository ticketReportProjectionRepository;

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
}
