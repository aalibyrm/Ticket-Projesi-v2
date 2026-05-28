package com.ticketmanagement.reporting.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;
import com.ticketmanagement.reporting.infrastructure.persistence.AgentPerformanceReportJdbcRepository;
import com.ticketmanagement.reporting.infrastructure.persistence.ClosedTicketReportJdbcRepository;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionJpaRepository;

@Service
@RequiredArgsConstructor
public class ReportingQueryService {

    private static final int MAX_CLOSED_TICKET_RANGE_DAYS = 366;
    private static final BigDecimal ZERO_MINUTES = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final TicketReportProjectionJpaRepository ticketReportProjectionRepository;
    private final ClosedTicketReportJdbcRepository closedTicketReportRepository;
    private final AgentPerformanceReportJdbcRepository agentPerformanceReportRepository;

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

    // Kapali ticket raporunu tarih araligi, gunluk dagilim ve priority kirilimiyla dondurur.
    @Transactional(readOnly = true)
    public ClosedTicketDateRangeReport getClosedTicketDateRangeReport(LocalDate fromDate, LocalDate toDate) {
        validateClosedTicketDateRange(fromDate, toDate);

        OffsetDateTime rangeStart = fromDate.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime rangeEndExclusive = toDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<ClosedTicketDailyCount> dailyCounts = closedTicketDailyCounts(fromDate, toDate, rangeStart, rangeEndExclusive);
        List<ClosedTicketPriorityCount> priorityCounts = closedTicketPriorityCounts(rangeStart, rangeEndExclusive);
        BigDecimal averageResolutionMinutes = closedTicketReportRepository
                .averageResolutionMinutes(rangeStart, rangeEndExclusive)
                .map(value -> value.setScale(2, RoundingMode.HALF_UP))
                .orElse(ZERO_MINUTES);
        long totalClosedTickets = dailyCounts.stream()
                .mapToLong(ClosedTicketDailyCount::count)
                .sum();

        return new ClosedTicketDateRangeReport(
                fromDate,
                toDate,
                totalClosedTickets,
                averageResolutionMinutes,
                dailyCounts,
                priorityCounts,
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    // Agent bazli atanmis/cozulmus ticket ve worklog surelerini manager raporu olarak dondurur.
    @Transactional(readOnly = true)
    public AgentPerformanceReport getAgentPerformanceReport() {
        return new AgentPerformanceReport(
                agentPerformanceReportRepository.agentPerformanceRows(),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    // Date range parametrelerini maliyetli rapor sorgularina karsi sinirlar.
    private void validateClosedTicketDateRange(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "fromDate must not be null");
        Objects.requireNonNull(toDate, "toDate must not be null");
        long rangeDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (rangeDays <= 0) {
            throw new InvalidReportRangeException("fromDate must be before or equal to toDate");
        }
        if (rangeDays > MAX_CLOSED_TICKET_RANGE_DAYS) {
            throw new InvalidReportRangeException(
                    "Date range must not exceed " + MAX_CLOSED_TICKET_RANGE_DAYS + " days");
        }
    }

    // Gunluk kapali ticket sayilarini eksik gunleri zero-count tamamlayarak uretir.
    private List<ClosedTicketDailyCount> closedTicketDailyCounts(
            LocalDate fromDate,
            LocalDate toDate,
            OffsetDateTime rangeStart,
            OffsetDateTime rangeEndExclusive) {
        Map<LocalDate, Long> countsByDate = new java.util.HashMap<>();
        closedTicketReportRepository.countClosedTicketsByDay(rangeStart, rangeEndExclusive)
                .forEach(count -> countsByDate.put(count.date(), count.count()));

        return fromDate.datesUntil(toDate.plusDays(1))
                .map(date -> new ClosedTicketDailyCount(date, countsByDate.getOrDefault(date, 0L)))
                .toList();
    }

    // Priority bazli kapali ticket sayilarini LOW/MEDIUM/HIGH sirasi ve zero-count ile uretir.
    private List<ClosedTicketPriorityCount> closedTicketPriorityCounts(
            OffsetDateTime rangeStart,
            OffsetDateTime rangeEndExclusive) {
        Map<ProjectionPriority, Long> countsByPriority = new EnumMap<>(ProjectionPriority.class);
        closedTicketReportRepository.countClosedTicketsByPriority(rangeStart, rangeEndExclusive)
                .forEach(count -> countsByPriority.put(count.priority(), count.count()));

        return Arrays.stream(ProjectionPriority.values())
                .map(priority -> new ClosedTicketPriorityCount(priority, countsByPriority.getOrDefault(priority, 0L)))
                .toList();
    }
}
