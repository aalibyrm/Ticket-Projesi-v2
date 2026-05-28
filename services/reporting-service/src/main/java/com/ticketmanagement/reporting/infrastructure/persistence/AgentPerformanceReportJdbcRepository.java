package com.ticketmanagement.reporting.infrastructure.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ticketmanagement.reporting.application.AgentPerformanceRow;

@Repository
@RequiredArgsConstructor
public class AgentPerformanceReportJdbcRepository {

    private static final BigDecimal ZERO_MINUTES = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final JdbcTemplate jdbcTemplate;

    public List<AgentPerformanceRow> agentPerformanceRows() {
        return jdbcTemplate.query(
                """
                        with assigned as (
                          select assignee_id as agent_id,
                                 count(*) as assigned_ticket_count,
                                 count(*) filter (where status in ('RESOLVED', 'CLOSED')) as resolved_ticket_count,
                                 avg(extract(epoch from (closed_at - opened_at)) / 60.0)
                                   filter (where status = 'CLOSED' and closed_at is not null) as avg_resolution_minutes
                          from reporting_schema.ticket_report_projection
                          where assignee_id is not null
                          group by assignee_id
                        ),
                        worklogs as (
                          select agent_id,
                                 sum(duration_minutes) as total_worklog_minutes
                          from reporting_schema.agent_worklog_projection
                          group by agent_id
                        ),
                        agents as (
                          select agent_id from assigned
                          union
                          select agent_id from worklogs
                        )
                        select agents.agent_id,
                               coalesce(assigned.assigned_ticket_count, 0) as assigned_ticket_count,
                               coalesce(assigned.resolved_ticket_count, 0) as resolved_ticket_count,
                               coalesce(worklogs.total_worklog_minutes, 0) as total_worklog_minutes,
                               assigned.avg_resolution_minutes
                        from agents
                        left join assigned on assigned.agent_id = agents.agent_id
                        left join worklogs on worklogs.agent_id = agents.agent_id
                        order by resolved_ticket_count desc, total_worklog_minutes desc, agent_id
                        """,
                (resultSet, rowNumber) -> new AgentPerformanceRow(
                        resultSet.getObject("agent_id", UUID.class),
                        resultSet.getLong("assigned_ticket_count"),
                        resultSet.getLong("resolved_ticket_count"),
                        resultSet.getLong("total_worklog_minutes"),
                        averageMinutes(resultSet.getBigDecimal("avg_resolution_minutes"))));
    }

    private static BigDecimal averageMinutes(BigDecimal value) {
        if (value == null) {
            return ZERO_MINUTES;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
