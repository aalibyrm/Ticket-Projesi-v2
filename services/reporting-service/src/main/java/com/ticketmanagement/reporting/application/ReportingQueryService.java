package com.ticketmanagement.reporting.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionJpaRepository;

@Service
@RequiredArgsConstructor
public class ReportingQueryService {

    private final TicketReportProjectionJpaRepository ticketReportProjectionRepository;

    // Acik ticket sayilarini status bazinda stabil sirayla raporlar.
    @Transactional(readOnly = true)
    public TicketStatusDistributionReport getOpenTicketStatusDistribution() {
        Map<ProjectionTicketStatus, Long> countsByStatus = new EnumMap<>(ProjectionTicketStatus.class);
        ticketReportProjectionRepository.countOpenTicketsByStatus(ProjectionTicketStatus.CLOSED)
                .forEach(count -> countsByStatus.put(count.status(), count.count()));

        List<TicketStatusCount> counts = ProjectionTicketStatus.openStatuses()
                .stream()
                .map(status -> new TicketStatusCount(status, countsByStatus.getOrDefault(status, 0L)))
                .toList();
        long totalOpenTickets = counts.stream()
                .mapToLong(TicketStatusCount::count)
                .sum();

        return new TicketStatusDistributionReport(counts, totalOpenTickets, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
