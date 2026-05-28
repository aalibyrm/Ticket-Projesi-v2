package com.ticketmanagement.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
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

import com.ticketmanagement.reporting.api.dto.TicketStatusDistributionResponse;
import com.ticketmanagement.reporting.application.ReportingProjectionService;
import com.ticketmanagement.reporting.application.TicketProjectionUpsertCommand;
import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ReportingStatusDistributionApiTests {

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
        jdbcTemplate.update("delete from reporting_schema.sla_compliance_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.agent_performance_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.ticket_status_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.ticket_report_projection");
        jdbcTemplate.update("delete from reporting_schema.processed_events");
    }

    @Test
    void managerCanRetrieveCurrentOpenTicketCountsByStatus() {
        createProjection("TCK-4001", ProjectionTicketStatus.NEW, null);
        createProjection("TCK-4002", ProjectionTicketStatus.NEW, null);
        createProjection("TCK-4003", ProjectionTicketStatus.IN_PROGRESS, null);
        createProjection("TCK-4004", ProjectionTicketStatus.CLOSED, OffsetDateTime.now(ZoneOffset.UTC));

        ResponseEntity<TicketStatusDistributionResponse> response = restTemplate.getForEntity(
                "/api/reports/tickets/status-distribution",
                TicketStatusDistributionResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalOpenTickets()).isEqualTo(3);
        assertThat(response.getBody().counts())
                .extracting(count -> count.status() + "=" + count.count())
                .containsExactly(
                        "NEW=2",
                        "IN_PROGRESS=1",
                        "WAITING_FOR_CUSTOMER=0",
                        "RESOLVED=0");
        assertThat(response.getBody().generatedAt()).isNotNull();
    }

    private void createProjection(String ticketNumber, ProjectionTicketStatus status, OffsetDateTime closedAt) {
        OffsetDateTime openedAt = OffsetDateTime.now(ZoneOffset.UTC).minusHours(3);
        OffsetDateTime updatedAt = closedAt == null ? openedAt.plusMinutes(10) : closedAt;
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                UUID.randomUUID(),
                ticketNumber,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ProjectionPriority.MEDIUM,
                status,
                null,
                null,
                openedAt,
                updatedAt,
                closedAt,
                openedAt.plusHours(24),
                status == ProjectionTicketStatus.CLOSED ? ProjectionSlaStatus.MET : ProjectionSlaStatus.ACTIVE));
    }
}
