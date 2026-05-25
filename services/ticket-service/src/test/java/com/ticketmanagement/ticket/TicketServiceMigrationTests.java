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
    }
}
