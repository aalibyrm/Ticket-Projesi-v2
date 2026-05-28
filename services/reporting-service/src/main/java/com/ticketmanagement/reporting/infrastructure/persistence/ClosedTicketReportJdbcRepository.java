package com.ticketmanagement.reporting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ticketmanagement.reporting.application.ClosedTicketDailyCount;
import com.ticketmanagement.reporting.application.ClosedTicketPriorityCount;
import com.ticketmanagement.reporting.domain.ProjectionPriority;

@Repository
@RequiredArgsConstructor
public class ClosedTicketReportJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<ClosedTicketDailyCount> countClosedTicketsByDay(
            OffsetDateTime rangeStart,
            OffsetDateTime rangeEndExclusive) {
        return jdbcTemplate.query(
                """
                        select (closed_at at time zone 'UTC')::date as closed_date,
                               count(*) as ticket_count
                        from reporting_schema.ticket_report_projection
                        where status = 'CLOSED'
                          and closed_at >= ?
                          and closed_at < ?
                        group by closed_date
                        order by closed_date
                        """,
                (resultSet, rowNumber) -> new ClosedTicketDailyCount(
                        resultSet.getObject("closed_date", LocalDate.class),
                        resultSet.getLong("ticket_count")),
                rangeStart,
                rangeEndExclusive);
    }

    public List<ClosedTicketPriorityCount> countClosedTicketsByPriority(
            OffsetDateTime rangeStart,
            OffsetDateTime rangeEndExclusive) {
        return jdbcTemplate.query(
                """
                        select priority,
                               count(*) as ticket_count
                        from reporting_schema.ticket_report_projection
                        where status = 'CLOSED'
                          and closed_at >= ?
                          and closed_at < ?
                        group by priority
                        order by priority
                        """,
                (resultSet, rowNumber) -> new ClosedTicketPriorityCount(
                        ProjectionPriority.from(resultSet.getString("priority")),
                        resultSet.getLong("ticket_count")),
                rangeStart,
                rangeEndExclusive);
    }

    public Optional<BigDecimal> averageResolutionMinutes(OffsetDateTime rangeStart, OffsetDateTime rangeEndExclusive) {
        BigDecimal average = jdbcTemplate.queryForObject(
                """
                        select avg(extract(epoch from (closed_at - opened_at)) / 60.0)
                        from reporting_schema.ticket_report_projection
                        where status = 'CLOSED'
                          and closed_at >= ?
                          and closed_at < ?
                        """,
                BigDecimal.class,
                rangeStart,
                rangeEndExclusive);
        return Optional.ofNullable(average);
    }
}
