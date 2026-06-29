package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ticketmanagement.ticket.api.dto.DepartmentResponse;
import com.ticketmanagement.ticket.api.dto.SupportTeamResponse;
import com.ticketmanagement.ticket.api.dto.TeamMemberResponse;
import com.ticketmanagement.ticket.api.error.ApiErrorResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class OrganizationApiIntegrationTests {

    private static final UUID IDENTITY_OPERATIONS_TEAM_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    private static final UUID INACTIVE_TEAM_ID = UUID.fromString("90000000-0000-0000-0000-000000000002");
    private static final UUID INACTIVE_ACTOR_ID = UUID.fromString("90000000-0000-0000-0000-000000000004");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("ticket_app")
            .withPassword("ticket_dev_password")
            .withInitScript("testdb/init-ticket-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void listsActiveDepartmentsWithSpecialistTeams() {
        insertInactiveOrganizationRecords();

        ResponseEntity<List<DepartmentResponse>> response = restTemplate.exchange(
                "/api/organization/departments",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(4);
        assertThat(response.getBody())
                .extracting(DepartmentResponse::code)
                .containsExactlyInAnyOrder(
                        "ACCESS_MANAGEMENT",
                        "APPLICATION_SUPPORT",
                        "INFRASTRUCTURE",
                        "FINANCE_OPERATIONS");

        List<SupportTeamResponse> teams = response.getBody().stream()
                .flatMap(department -> department.teams().stream())
                .toList();

        assertThat(teams).hasSize(8);
        assertThat(teams)
                .extracting(SupportTeamResponse::code)
                .containsExactlyInAnyOrder(
                        "IDENTITY_OPERATIONS",
                        "PERMISSION_OPERATIONS",
                        "WEB_APP_SUPPORT",
                        "CORE_APP_SUPPORT",
                        "NETWORK_OPERATIONS",
                        "PLATFORM_OPERATIONS",
                        "BILLING_OPERATIONS",
                        "PAYMENT_OPERATIONS");
        assertThat(teams)
                .extracting(SupportTeamResponse::code)
                .noneMatch(code -> code.contains("TRIAGE"));
        assertThat(teams)
                .extracting(SupportTeamResponse::leadActorId)
                .doesNotContainNull();
    }

    @Test
    void listsActiveTeamMembersAndHidesInactiveMemberships() {
        insertInactiveOrganizationRecords();

        ResponseEntity<List<TeamMemberResponse>> response = restTemplate.exchange(
                "/api/organization/teams/{teamId}/members",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {
                },
                IDENTITY_OPERATIONS_TEAM_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).filteredOn(TeamMemberResponse::teamLead).hasSize(1);
        assertThat(response.getBody())
                .extracting(TeamMemberResponse::actorId)
                .doesNotContain(INACTIVE_ACTOR_ID);
        assertThat(response.getBody())
                .extracting(TeamMemberResponse::displayName)
                .contains("Irem Gunes", "Elif Aydin");
        assertThat(response.getBody())
                .extracting(TeamMemberResponse::email)
                .contains("irem.gunes@example.local", "elif.aydin@example.local");
    }

    @Test
    void rejectsInactiveTeamMemberLookupAsNotFound() {
        insertInactiveOrganizationRecords();

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                "/api/organization/teams/{teamId}/members",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                ApiErrorResponse.class,
                INACTIVE_TEAM_ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("RESOURCE_NOT_FOUND");
    }

    private void insertInactiveOrganizationRecords() {
        jdbcTemplate.update(
                """
                        insert into ticket_schema.departments (id, code, name, active)
                        values ('90000000-0000-0000-0000-000000000001', 'LEGACY_SUPPORT', 'Legacy Support', false)
                        on conflict (code) do nothing
                        """);
        jdbcTemplate.update(
                """
                        insert into ticket_schema.support_teams (id, department_id, code, name, lead_actor_id, active)
                        values (
                          '90000000-0000-0000-0000-000000000002',
                          '90000000-0000-0000-0000-000000000001',
                          'LEGACY_TRIAGE',
                          'Legacy Triage',
                          '90000000-0000-0000-0000-000000000003',
                          false
                        )
                        on conflict (code) do nothing
                        """);
        jdbcTemplate.update(
                """
                        insert into ticket_schema.team_members (id, team_id, actor_id, team_lead, active)
                        values (
                          '90000000-0000-0000-0000-000000000004',
                          '20000000-0000-0000-0000-000000000001',
                          '90000000-0000-0000-0000-000000000004',
                          false,
                          false
                        )
                        on conflict (team_id, actor_id) do nothing
                        """);
    }
}
