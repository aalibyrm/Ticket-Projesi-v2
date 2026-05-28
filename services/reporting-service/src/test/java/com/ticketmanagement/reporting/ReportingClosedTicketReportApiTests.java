package com.ticketmanagement.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

import com.ticketmanagement.reporting.api.dto.ClosedTicketDateRangeResponse;
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
class ReportingClosedTicketReportApiTests {

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
        jdbcTemplate.update("delete from reporting_schema.sla_compliance_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.agent_performance_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.ticket_status_daily_projection");
        jdbcTemplate.update("delete from reporting_schema.ticket_report_projection");
        jdbcTemplate.update("delete from reporting_schema.processed_events");
    }

    @Test
    void managerCanRetrieveClosedTicketAggregatesForValidDateRange() throws Exception {
        createClosedProjection("TCK-4101", ProjectionPriority.LOW, "2026-05-27T08:00:00Z", "2026-05-27T10:00:00Z");
        createClosedProjection("TCK-4102", ProjectionPriority.MEDIUM, "2026-05-27T09:00:00Z", "2026-05-27T10:00:00Z");
        createClosedProjection("TCK-4103", ProjectionPriority.HIGH, "2026-05-28T09:00:00Z", "2026-05-28T11:00:00Z");
        createClosedProjection("TCK-4104", ProjectionPriority.HIGH, "2026-05-29T09:00:00Z", "2026-05-29T11:00:00Z");
        createOpenProjection("TCK-4105", ProjectionPriority.HIGH);

        String responseBody = mockMvc.perform(get("/api/reports/tickets/closed")
                        .with(managerJwt())
                        .param("fromDate", "2026-05-27")
                        .param("toDate", "2026-05-28"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ClosedTicketDateRangeResponse response = objectMapper.readValue(
                responseBody,
                ClosedTicketDateRangeResponse.class);

        assertThat(response.fromDate()).isEqualTo(LocalDate.parse("2026-05-27"));
        assertThat(response.toDate()).isEqualTo(LocalDate.parse("2026-05-28"));
        assertThat(response.totalClosedTickets()).isEqualTo(3);
        assertThat(response.averageResolutionMinutes()).isEqualByComparingTo("100.00");
        assertThat(response.dailyCounts())
                .extracting(count -> count.date() + "=" + count.count())
                .containsExactly("2026-05-27=2", "2026-05-28=1");
        assertThat(response.priorityCounts())
                .extracting(count -> count.priority() + "=" + count.count())
                .containsExactly("LOW=1", "MEDIUM=1", "HIGH=1");
        assertThat(response.generatedAt()).isNotNull();
    }

    @Test
    void invalidDateRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/reports/tickets/closed")
                        .with(managerJwt())
                        .param("fromDate", "2026-05-29")
                        .param("toDate", "2026-05-28"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void customerJwtCannotRetrieveClosedTicketReport() throws Exception {
        mockMvc.perform(get("/api/reports/tickets/closed")
                        .with(customerJwt())
                        .param("fromDate", "2026-05-27")
                        .param("toDate", "2026-05-28"))
                .andExpect(status().isForbidden());
    }

    private void createClosedProjection(
            String ticketNumber,
            ProjectionPriority priority,
            String openedAt,
            String closedAt) {
        OffsetDateTime opened = OffsetDateTime.parse(openedAt);
        OffsetDateTime closed = OffsetDateTime.parse(closedAt);
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                UUID.randomUUID(),
                ticketNumber,
                UUID.randomUUID(),
                UUID.randomUUID(),
                priority,
                ProjectionTicketStatus.CLOSED,
                UUID.randomUUID(),
                UUID.randomUUID(),
                opened,
                closed,
                closed,
                opened.plusHours(24),
                ProjectionSlaStatus.MET));
    }

    private void createOpenProjection(String ticketNumber, ProjectionPriority priority) {
        OffsetDateTime openedAt = OffsetDateTime.parse("2026-05-27T12:00:00Z");
        reportingProjectionService.upsertTicketProjection(new TicketProjectionUpsertCommand(
                UUID.randomUUID(),
                ticketNumber,
                UUID.randomUUID(),
                UUID.randomUUID(),
                priority,
                ProjectionTicketStatus.IN_PROGRESS,
                null,
                null,
                openedAt,
                openedAt.plusMinutes(5),
                null,
                openedAt.plusHours(24),
                ProjectionSlaStatus.ACTIVE));
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
