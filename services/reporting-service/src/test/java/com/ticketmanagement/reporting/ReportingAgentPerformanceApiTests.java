package com.ticketmanagement.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.reporting.api.dto.AgentSummaryResponse;
import com.ticketmanagement.reporting.api.dto.AgentPerformanceReportResponse;
import com.ticketmanagement.reporting.application.AgentWorklogProjectionCommand;
import com.ticketmanagement.reporting.application.ReportingProjectionService;
import com.ticketmanagement.reporting.application.TicketProjectionUpsertCommand;
import com.ticketmanagement.reporting.domain.ProjectionPriority;
import com.ticketmanagement.reporting.domain.ProjectionSlaStatus;
import com.ticketmanagement.reporting.domain.ProjectionTicketStatus;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.security.jwt.enabled=true")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class ReportingAgentPerformanceApiTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("reporting_app")
            .withPassword("reporting_dev_password")
            .withInitScript("testdb/init-reporting-schema.sql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReportingProjectionService reportingProjectionService;

    @TestConfiguration
    static class JwtDecoderTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new UnsupportedOperationException("MockMvc jwt() supplies authentication in these tests");
            };
        }
    }

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
    void managerCanRetrieveAgentPerformanceTable() throws Exception {
        UUID agentOne = UUID.randomUUID();
        UUID agentTwo = UUID.randomUUID();
        UUID agentThree = UUID.randomUUID();
        UUID closedTicketForAgentOne = createTicketProjection(
                "TCK-4201",
                agentOne,
                ProjectionTicketStatus.CLOSED,
                "2026-05-27T08:00:00Z",
                "2026-05-27T10:00:00Z");
        UUID resolvedTicketForAgentOne = createTicketProjection(
                "TCK-4202",
                agentOne,
                ProjectionTicketStatus.RESOLVED,
                "2026-05-27T08:00:00Z",
                null);
        createTicketProjection(
                "TCK-4203",
                agentOne,
                ProjectionTicketStatus.IN_PROGRESS,
                "2026-05-27T08:00:00Z",
                null);
        createTicketProjection(
                "TCK-4204",
                agentTwo,
                ProjectionTicketStatus.CLOSED,
                "2026-05-27T08:00:00Z",
                "2026-05-27T09:00:00Z");

        createWorklog("TCK-4201", closedTicketForAgentOne, agentOne, 30);
        createWorklog("TCK-4202", resolvedTicketForAgentOne, agentOne, 45);
        createWorklog("TCK-4201", closedTicketForAgentOne, agentTwo, 15);
        createWorklog("TCK-4201", closedTicketForAgentOne, agentThree, 60);

        String responseBody = mockMvc.perform(get("/api/reports/agents/performance")
                        .with(managerJwt()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AgentPerformanceReportResponse response = objectMapper.readValue(
                responseBody,
                AgentPerformanceReportResponse.class);

        assertThat(response.rows())
                .extracting(row -> row.agentId()
                        + "|assigned=" + row.assignedTicketCount()
                        + "|resolved=" + row.resolvedTicketCount()
                        + "|worklog=" + row.totalWorklogMinutes()
                        + "|avg=" + row.averageResolutionMinutes())
                .containsExactly(
                        agentOne + "|assigned=3|resolved=2|worklog=75|avg=120.00",
                        agentTwo + "|assigned=1|resolved=1|worklog=15|avg=60.00",
                        agentThree + "|assigned=0|resolved=0|worklog=60|avg=0.00");
        assertThat(response.generatedAt()).isNotNull();
    }

    @Test
    void customerJwtCannotRetrieveAgentPerformanceTable() throws Exception {
        mockMvc.perform(get("/api/reports/agents/performance")
                        .with(customerJwt()))
                .andExpect(status().isForbidden());
    }

    @Test
    void internalAgentSummaryReturnsResolvedAndSlaCounts() throws Exception {
        UUID agentId = UUID.randomUUID();
        createTicketProjection(
                "TCK-4211",
                agentId,
                ProjectionTicketStatus.CLOSED,
                "2026-05-27T08:00:00Z",
                "2026-05-27T10:00:00Z",
                ProjectionSlaStatus.MET);
        createTicketProjection(
                "TCK-4212",
                agentId,
                ProjectionTicketStatus.CLOSED,
                "2026-05-27T08:00:00Z",
                "2026-05-27T11:00:00Z",
                ProjectionSlaStatus.BREACHED);
        createTicketProjection(
                "TCK-4213",
                agentId,
                ProjectionTicketStatus.RESOLVED,
                "2026-05-27T08:00:00Z",
                null,
                ProjectionSlaStatus.ACTIVE);
        createTicketProjection(
                "TCK-4214",
                UUID.randomUUID(),
                ProjectionTicketStatus.CLOSED,
                "2026-05-27T08:00:00Z",
                "2026-05-27T09:00:00Z",
                ProjectionSlaStatus.MET);

        String responseBody = mockMvc.perform(get("/internal/reports/agents/{agentId}/summary", agentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AgentSummaryResponse response = objectMapper.readValue(responseBody, AgentSummaryResponse.class);
        assertThat(response.agentId()).isEqualTo(agentId);
        assertThat(response.resolvedTicketCount()).isEqualTo(3);
        assertThat(response.slaMetTicketCount()).isEqualTo(1);
        assertThat(response.slaBreachedTicketCount()).isEqualTo(1);
        assertThat(response.slaCompliancePercentage()).isEqualByComparingTo("50.00");
    }

    @Test
    void publicOpenApiDoesNotExposeInternalReports() throws Exception {
        String responseBody = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(responseBody).doesNotContain("/internal/reports");
        assertThat(responseBody).contains("/api/v1/reports/agents/performance");
    }

    private UUID createTicketProjection(
            String ticketNumber,
            UUID assigneeId,
            ProjectionTicketStatus status,
            String openedAt,
            String closedAt) {
        return createTicketProjection(
                ticketNumber,
                assigneeId,
                status,
                openedAt,
                closedAt,
                status == ProjectionTicketStatus.CLOSED ? ProjectionSlaStatus.MET : ProjectionSlaStatus.ACTIVE);
    }

    private UUID createTicketProjection(
            String ticketNumber,
            UUID assigneeId,
            ProjectionTicketStatus status,
            String openedAt,
            String closedAt,
            ProjectionSlaStatus slaStatus) {
        UUID ticketId = UUID.randomUUID();
        OffsetDateTime opened = OffsetDateTime.parse(openedAt);
        OffsetDateTime closed = closedAt == null ? null : OffsetDateTime.parse(closedAt);
        OffsetDateTime updatedAt = closed == null ? opened.plusMinutes(10) : closed;
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                ticketId,
                ticketNumber,
                UUID.randomUUID(),
                UUID.randomUUID(),
                ProjectionPriority.HIGH,
                status,
                assigneeId,
                UUID.randomUUID(),
                opened,
                updatedAt,
                closed,
                opened.plusHours(8),
                slaStatus));
        return ticketId;
    }

    private void createWorklog(String ticketNumber, UUID ticketId, UUID agentId, int durationMinutes) {
        reportingProjectionService.upsertAgentWorklogProjection(new AgentWorklogProjectionCommand(
                UUID.randomUUID(),
                ticketId,
                ticketNumber,
                agentId,
                LocalDate.parse("2026-05-27"),
                durationMinutes));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor managerJwt() {
        return jwt()
                .jwt(builder -> builder
                        .subject(UUID.randomUUID().toString())
                        .claim("realm_access", Map.of("roles", List.of("MANAGER"))))
                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor customerJwt() {
        return jwt()
                .jwt(builder -> builder
                        .subject(UUID.randomUUID().toString())
                        .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }
}
