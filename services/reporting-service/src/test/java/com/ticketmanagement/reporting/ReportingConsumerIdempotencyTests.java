package com.ticketmanagement.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.event.ticket.TicketCreatedPayload;
import com.ticketmanagement.event.ticket.WorklogAddedPayload;
import com.ticketmanagement.event.workflow.SlaBreachedPayload;
import com.ticketmanagement.reporting.application.AgentPerformanceReport;
import com.ticketmanagement.reporting.application.ReportingQueryService;
import com.ticketmanagement.reporting.application.ReportingTicketEventProjectionService;
import com.ticketmanagement.reporting.application.ReportingWorkflowEventProjectionService;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.infrastructure.kafka.ConsumedEvent;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionEntity;
import com.ticketmanagement.reporting.infrastructure.persistence.TicketReportProjectionJpaRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ReportingConsumerIdempotencyTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("reporting_app")
            .withPassword("reporting_dev_password")
            .withInitScript("testdb/init-reporting-schema.sql");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReportingTicketEventProjectionService ticketEventProjectionService;

    @Autowired
    private ReportingWorkflowEventProjectionService workflowEventProjectionService;

    @Autowired
    private ReportingQueryService reportingQueryService;

    @Autowired
    private TicketReportProjectionJpaRepository ticketReportProjectionRepository;

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
    void duplicateTicketCreatedEventCreatesProjectionOnlyOnce() {
        UUID ticketId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        ConsumedEvent event = ticketEvent(
                eventId,
                "ticket.created",
                ticketId,
                new TicketCreatedPayload(
                        ticketId,
                        "TCK-4401",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "HIGH",
                        "NEW"));

        assertThat(ticketEventProjectionService.handleTicketEvent(event)).isTrue();
        assertThat(ticketEventProjectionService.handleTicketEvent(event)).isFalse();

        assertThat(ticketReportProjectionRepository.findAll())
                .singleElement()
                .extracting(TicketReportProjectionEntity::getTicketNumber)
                .isEqualTo("TCK-4401");
        assertThat(processedEventCount("reporting-service.ticket-events")).isEqualTo(1);
    }

    @Test
    void duplicateWorklogEventDoesNotDoubleCountAgentPerformance() {
        UUID ticketId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        ticketEventProjectionService.handleTicketEvent(ticketEvent(
                UUID.randomUUID(),
                "ticket.created",
                ticketId,
                new TicketCreatedPayload(
                        ticketId,
                        "TCK-4402",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "MEDIUM",
                        "NEW")));
        ConsumedEvent worklogEvent = ticketEvent(
                UUID.randomUUID(),
                "ticket.worklog-added",
                ticketId,
                new WorklogAddedPayload(
                        ticketId,
                        "TCK-4402",
                        UUID.randomUUID(),
                        agentId,
                        LocalDate.parse("2026-05-28"),
                        30));

        assertThat(ticketEventProjectionService.handleTicketEvent(worklogEvent)).isTrue();
        assertThat(ticketEventProjectionService.handleTicketEvent(worklogEvent)).isFalse();

        AgentPerformanceReport report = reportingQueryService.getAgentPerformanceReport();

        assertThat(report.rows())
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.agentId()).isEqualTo(agentId);
                    assertThat(row.totalWorklogMinutes()).isEqualTo(30);
                });
        assertThat(processedEventCount("reporting-service.ticket-events")).isEqualTo(2);
    }

    @Test
    void duplicateWorkflowBreachEventUpdatesSlaProjectionOnlyOnce() {
        UUID ticketId = UUID.randomUUID();
        ticketEventProjectionService.handleTicketEvent(ticketEvent(
                UUID.randomUUID(),
                "ticket.created",
                ticketId,
                new TicketCreatedPayload(
                        ticketId,
                        "TCK-4403",
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "HIGH",
                        "NEW")));
        Instant targetResolutionAt = Instant.parse("2026-05-28T18:00:00Z");
        ConsumedEvent breachEvent = workflowEvent(
                UUID.randomUUID(),
                "workflow.sla-breach-detected",
                ticketId,
                new SlaBreachedPayload(
                        ticketId,
                        "TCK-4403",
                        UUID.randomUUID(),
                        "HIGH",
                        targetResolutionAt,
                        Instant.parse("2026-05-28T19:00:00Z"),
                        "target-resolution-expired"));

        assertThat(workflowEventProjectionService.handleWorkflowEvent(breachEvent)).isTrue();
        assertThat(workflowEventProjectionService.handleWorkflowEvent(breachEvent)).isFalse();

        assertThat(ticketReportProjectionRepository.findById(ticketId))
                .isPresent()
                .get()
                .extracting(TicketReportProjectionEntity::getSlaStatus)
                .isEqualTo(ProjectionSlaStatus.BREACHED);
        assertThat(processedEventCount("reporting-service.workflow-events")).isEqualTo(1);
    }

    private ConsumedEvent ticketEvent(UUID eventId, String eventType, UUID ticketId, Object payload) {
        return consumedEvent(eventId, eventType, "ticket", ticketId, payload);
    }

    private ConsumedEvent workflowEvent(UUID eventId, String eventType, UUID ticketId, Object payload) {
        return consumedEvent(eventId, eventType, "sla", ticketId, payload);
    }

    private ConsumedEvent consumedEvent(
            UUID eventId,
            String eventType,
            String aggregateType,
            UUID aggregateId,
            Object payload) {
        JsonNode payloadJson = objectMapper.valueToTree(payload);
        return new ConsumedEvent(
                eventId,
                eventType,
                1,
                OffsetDateTime.of(2026, 5, 28, 12, 0, 0, 0, ZoneOffset.UTC),
                UUID.randomUUID(),
                aggregateType,
                aggregateId,
                "corr-" + aggregateId,
                payloadJson);
    }

    private Integer processedEventCount(String consumerName) {
        return jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from reporting_schema.processed_events
                        where consumer_name = ?
                        """,
                Integer.class,
                consumerName);
    }
}
