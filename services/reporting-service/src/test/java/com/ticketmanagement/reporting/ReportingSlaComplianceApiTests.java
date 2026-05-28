package com.ticketmanagement.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.ticketmanagement.reporting.api.dto.SlaComplianceReportResponse;
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
class ReportingSlaComplianceApiTests {

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
    void managerCanRetrieveSlaCompliancePercentage() throws Exception {
        createProjection("TCK-4301", ProjectionPriority.LOW, ProjectionSlaStatus.MET);
        createProjection("TCK-4302", ProjectionPriority.LOW, ProjectionSlaStatus.ACTIVE);
        createProjection("TCK-4303", ProjectionPriority.MEDIUM, ProjectionSlaStatus.AT_RISK);
        createProjection("TCK-4304", ProjectionPriority.HIGH, ProjectionSlaStatus.MET);
        createProjection("TCK-4305", ProjectionPriority.HIGH, ProjectionSlaStatus.BREACHED);
        createProjection("TCK-4306", ProjectionPriority.HIGH, null);

        String responseBody = mockMvc.perform(get("/api/reports/sla/compliance")
                        .with(managerJwt()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        SlaComplianceReportResponse response = objectMapper.readValue(responseBody, SlaComplianceReportResponse.class);

        assertThat(response.metTicketCount()).isEqualTo(2);
        assertThat(response.breachedTicketCount()).isEqualTo(1);
        assertThat(response.atRiskTicketCount()).isEqualTo(1);
        assertThat(response.activeTicketCount()).isEqualTo(1);
        assertThat(response.compliancePercentage()).isEqualByComparingTo("66.67");
        assertThat(response.priorityBreakdown())
                .extracting(priority -> priority.priority()
                        + "|met=" + priority.metTicketCount()
                        + "|breached=" + priority.breachedTicketCount()
                        + "|risk=" + priority.atRiskTicketCount()
                        + "|active=" + priority.activeTicketCount()
                        + "|pct=" + priority.compliancePercentage())
                .containsExactly(
                        "LOW|met=1|breached=0|risk=0|active=1|pct=100.00",
                        "MEDIUM|met=0|breached=0|risk=1|active=0|pct=0.00",
                        "HIGH|met=1|breached=1|risk=0|active=0|pct=50.00");
        assertThat(response.generatedAt()).isNotNull();
    }

    @Test
    void customerJwtCannotRetrieveSlaComplianceReport() throws Exception {
        mockMvc.perform(get("/api/reports/sla/compliance")
                        .with(customerJwt()))
                .andExpect(status().isForbidden());
    }

    private void createProjection(String ticketNumber, ProjectionPriority priority, ProjectionSlaStatus slaStatus) {
        OffsetDateTime openedAt = OffsetDateTime.parse("2026-05-27T08:00:00Z");
        boolean completed = slaStatus == ProjectionSlaStatus.MET || slaStatus == ProjectionSlaStatus.BREACHED;
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                UUID.randomUUID(),
                ticketNumber,
                UUID.randomUUID(),
                UUID.randomUUID(),
                priority,
                completed ? ProjectionTicketStatus.CLOSED : ProjectionTicketStatus.IN_PROGRESS,
                UUID.randomUUID(),
                UUID.randomUUID(),
                openedAt,
                completed ? openedAt.plusHours(2) : openedAt.plusMinutes(10),
                completed ? openedAt.plusHours(2) : null,
                openedAt.plusHours(8),
                slaStatus));
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
