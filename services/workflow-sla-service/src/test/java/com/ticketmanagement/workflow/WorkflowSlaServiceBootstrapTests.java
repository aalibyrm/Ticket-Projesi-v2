package com.ticketmanagement.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.kogito.process.Processes;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class WorkflowSlaServiceBootstrapTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("workflow_app")
            .withPassword("workflow_dev_password")
            .withInitScript("testdb/init-workflow-schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Processes processes;

    @Test
    void startsWithHealthEndpointSchemaBaselineAndKogitoRuntime() {
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity("/actuator/health", Map.class);

        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).containsEntry("status", "UP");
        assertThat(tableExists("service_metadata")).isEqualTo(1);
        assertThat(tableExists("processed_events")).isEqualTo(1);
        assertThat(tableExists("sla_ticket_states")).isEqualTo(1);
        assertThat(tableExists("outbox_events")).isEqualTo(1);
        assertThat(metadataValue("service_name")).isEqualTo("workflow-sla-service");
        assertThat(metadataValue("workflow_runtime")).isEqualTo("kogito");
        assertThat(processes.processIds()).contains("runtimeSmoke", "ticketLifecycle");
    }

    private Integer tableExists(String tableName) {
        return jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from information_schema.tables
                        where table_schema = 'workflow_schema'
                          and table_name = ?
                        """,
                Integer.class,
                tableName);
    }

    private String metadataValue(String metadataKey) {
        return jdbcTemplate.queryForObject(
                """
                        select metadata_value
                        from workflow_schema.service_metadata
                        where metadata_key = ?
                        """,
                String.class,
                metadataKey);
    }
}
