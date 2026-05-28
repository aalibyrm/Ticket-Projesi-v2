package com.ticketmanagement.reporting.infrastructure.persistence;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ticketmanagement.reporting.application.SlaComplianceStatusCount;
import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;

@Repository
@RequiredArgsConstructor
public class SlaComplianceReportJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<SlaComplianceStatusCount> countTicketsByPriorityAndSlaStatus() {
        return jdbcTemplate.query(
                """
                        select priority,
                               sla_status,
                               count(*) as ticket_count
                        from reporting_schema.ticket_report_projection
                        where sla_status in ('ACTIVE', 'AT_RISK', 'BREACHED', 'MET')
                        group by priority, sla_status
                        order by priority, sla_status
                        """,
                (resultSet, rowNumber) -> new SlaComplianceStatusCount(
                        ProjectionPriority.from(resultSet.getString("priority")),
                        ProjectionSlaStatus.from(resultSet.getString("sla_status")),
                        resultSet.getLong("ticket_count")));
    }
}
