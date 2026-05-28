package com.ticketmanagement.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.reporting.application.ReportingProjectionService;
import com.ticketmanagement.reporting.application.TicketProjectionUpsertCommand;
import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ReportingServiceBootstrapTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("reporting_app")
            .withPassword("reporting_dev_password")
            .withInitScript("testdb/init-reporting-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReportingProjectionService reportingProjectionService;

    @BeforeEach
    void cleanReportingData() {
        jdbcTemplate.update("delete from reporting_schema.agent_worklog_projection");
        jdbcTemplate.update("delete from reporting_schema.sla_compliance_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.agent_performance_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.ticket_status_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.ticket_report_projection");
        jdbcTemplate.update("delete from reporting_schema.processed_events");
    }

    @Test
    void startsWithHealthEndpointAndReportingSchemaMigrations() {
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).containsEntry("status", "UP");
        assertThat(tableExists("processed_events")).isEqualTo(1);
        assertThat(tableExists("ticket_report_projection")).isEqualTo(1);
        assertThat(tableExists("ticket_status_daily_projection")).isEqualTo(1);
        assertThat(tableExists("agent_performance_daily_projection")).isEqualTo(1);
        assertThat(tableExists("sla_compliance_daily_projection")).isEqualTo(1);
        assertThat(tableExists("agent_worklog_projection")).isEqualTo(1);
        assertThat(serviceName()).isEqualTo("reporting-service");
    }

    @Test
    void ticketReportProjectionCanPersistAndUpdate() {
        UUID ticketId = UUID.randomUUID();
        OffsetDateTime openedAt = OffsetDateTime.now(ZoneOffset.UTC).minusHours(2);
        OffsetDateTime firstUpdateAt = openedAt.plusMinutes(5);
        OffsetDateTime targetResolutionAt = openedAt.plusHours(8);

        TicketReportProjectionEntity createdProjection = reportingProjectionService.upsertTicketProjection(
                new TicketProjectionUpsertCommand(
                        ticketId,
                        "TCK-3901",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ProjectionPriority.HIGH,
                        ProjectionTicketStatus.NEW,
                        null,
                        null,
                        openedAt,
                        firstUpdateAt,
                        null,
                        targetResolutionAt,
                        ProjectionSlaStatus.ACTIVE));

        assertThat(createdProjection.getTicketId()).isEqualTo(ticketId);
        assertThat(createdProjection.getStatus()).isEqualTo(ProjectionTicketStatus.NEW);
        assertThat(countTicketProjectionRows()).isEqualTo(1);

        OffsetDateTime closedAt = openedAt.plusHours(1);
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                ticketId,
                "TCK-3901",
                createdProjection.getCustomerId(),
                createdProjection.getProductId(),
                ProjectionPriority.HIGH,
                ProjectionTicketStatus.CLOSED,
                UUID.randomUUID(),
                UUID.randomUUID(),
                openedAt,
                closedAt,
                closedAt,
                targetResolutionAt,
                ProjectionSlaStatus.MET));

        TicketReportProjectionEntity updatedProjection = reportingProjectionService.findTicketProjection(ticketId)
                .orElseThrow();

        assertThat(updatedProjection.getStatus()).isEqualTo(ProjectionTicketStatus.CLOSED);
        assertThat(updatedProjection.getSlaStatus()).isEqualTo(ProjectionSlaStatus.MET);
        assertThat(updatedProjection.getClosedAt()).isNotNull();
        assertThat(countTicketProjectionRows()).isEqualTo(1);
    }

    private Integer tableExists(String tableName) {
        return jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'reporting_schema'
                          and table_name = ?
                        """,
                Integer.class,
                tableName);
    }

    private String serviceName() {
        return jdbcTemplate.queryForObject(
                """
                        select metadata_value
                        from reporting_schema.service_metadata
                        where metadata_key = 'service_name'
                        """,
                String.class);
    }

    private Integer countTicketProjectionRows() {
        return jdbcTemplate.queryForObject(
                "select count(*) from reporting_schema.ticket_report_projection",
                Integer.class);
    }
}
