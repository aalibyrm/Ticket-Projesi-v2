package com.ticketmanagement.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class TicketServiceMigrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("ticket_app")
            .withPassword("ticket_dev_password")
            .withInitScript("testdb/init-ticket-schema.sql");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void appliesTicketServiceBaselineMigration() {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.service_metadata where metadata_key = 'service_name'",
                Integer.class);

        assertThat(count).isEqualTo(1);

        Integer productCount = jdbcTemplate.queryForObject("select count(*) from ticket_schema.products", Integer.class);

        assertThat(productCount).isGreaterThanOrEqualTo(3);

        Integer outboxTableCount = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'ticket_schema' and table_name = 'outbox_events'",
                Integer.class);

        assertThat(outboxTableCount).isEqualTo(1);

        Integer processingStatusAllowed = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from pg_constraint
                        where conname = 'outbox_events_status_check'
                          and pg_get_constraintdef(oid) like '%PROCESSING%'
                        """,
                Integer.class);

        assertThat(processingStatusAllowed).isEqualTo(1);

        Integer lifecycleTableCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'ticket_schema'
                          and table_name in ('ticket_comments', 'ticket_worklogs', 'ticket_conversation_reads')
                        """,
                Integer.class);

        assertThat(lifecycleTableCount).isEqualTo(3);

        Integer assignmentColumnCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.columns
                        where table_schema = 'ticket_schema'
                          and table_name = 'tickets'
                          and column_name in ('assignee_id', 'assigned_team_id')
                        """,
                Integer.class);

        assertThat(assignmentColumnCount).isEqualTo(2);

        Integer organizationTableCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'ticket_schema'
                          and table_name in ('departments', 'support_teams', 'team_members')
                        """,
                Integer.class);

        assertThat(organizationTableCount).isEqualTo(3);

        Integer departmentCount = jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.departments where active = true",
                Integer.class);

        assertThat(departmentCount).isEqualTo(4);

        Integer activeTeamCount = jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.support_teams where active = true",
                Integer.class);

        assertThat(activeTeamCount).isEqualTo(9);

        Integer activeLeadCount = jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.team_members where active = true and team_lead = true",
                Integer.class);

        assertThat(activeLeadCount).isEqualTo(9);

        Integer routingTableCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'ticket_schema'
                          and table_name in ('ticket_topics', 'ticket_routing_rules', 'ticket_routing_cursors')
                        """,
                Integer.class);

        assertThat(routingTableCount).isEqualTo(3);

        Integer routeColumnCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.columns
                        where table_schema = 'ticket_schema'
                          and table_name = 'tickets'
                          and column_name in ('topic_id', 'routed_department_id')
                        """,
                Integer.class);

        assertThat(routeColumnCount).isEqualTo(2);

        Integer activeTopicCount = jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.ticket_topics where active = true",
                Integer.class);

        assertThat(activeTopicCount).isEqualTo(8);

        Integer activeRoutingRuleCount = jdbcTemplate.queryForObject(
                "select count(*) from ticket_schema.ticket_routing_rules where active = true",
                Integer.class);

        assertThat(activeRoutingRuleCount).isEqualTo(9);

        Integer paymentRouteCount = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from ticket_schema.ticket_routing_rules rule
                        join ticket_schema.ticket_topics topic on topic.id = rule.topic_id
                        where topic.code = 'PAYMENT_FAILURE'
                          and rule.active = true
                        """,
                Integer.class);

        assertThat(paymentRouteCount).isEqualTo(2);
    }
}
