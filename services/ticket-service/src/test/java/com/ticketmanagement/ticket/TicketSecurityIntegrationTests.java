package com.ticketmanagement.ticket;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductJpaRepository;

@SpringBootTest(properties = "app.security.jwt.enabled=true")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TicketSecurityIntegrationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("ticket_platform")
            .withUsername("ticket_app")
            .withPassword("ticket_dev_password")
            .withInitScript("testdb/init-ticket-schema.sql");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductJpaRepository productRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void rejectsUnauthenticatedTicketApiRequest() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsAuthenticatedJwtForProductApiRequest() throws Exception {
        mockMvc.perform(get("/api/products")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAgentRoleOnCustomerTicketEndpoint() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .with(jwt().jwt(token -> token
                                .subject(UUID.randomUUID().toString())
                                .claim("realm_access", Map.of("roles", List.of("AGENT"))))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void usesJwtSubjectInsteadOfSpoofedActorHeader() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID spoofedActorId = UUID.randomUUID();
        UUID productId = productRepository.findByActiveTrueOrderByNameAsc().getFirst().getId();
        CreateTicketRequest request = new CreateTicketRequest(
                productId,
                "Cannot open invoice",
                "Invoice page returns a blank screen after login.",
                TicketPriority.MEDIUM);

        mockMvc.perform(post("/api/tickets")
                        .header("X-Actor-Id", spoofedActorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token
                                .subject(customerId.toString())
                                .claim("realm_access", Map.of("roles", List.of("CUSTOMER"))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
    }
}
