package com.ticketmanagement.ticket;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.AddInternalNoteRequest;
import com.ticketmanagement.ticket.api.dto.AddWorklogRequest;
import com.ticketmanagement.ticket.api.dto.AssignTicketRequest;
import com.ticketmanagement.ticket.api.dto.ChangeTicketStatusRequest;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.domain.TicketPriority;
import com.ticketmanagement.ticket.domain.TicketStatus;
import com.ticketmanagement.ticket.infrastructure.persistence.ProductJpaRepository;

@SpringBootTest(properties = "app.security.jwt.enabled=true")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class TicketSecurityIntegrationTests {

    private static final String DEFAULT_TOPIC_CODE = "WEB_PORTAL_BUG";
    private static final String CORE_TOPIC_CODE = "CORE_SYSTEM_ERROR";
    private static final UUID WEB_APP_SUPPORT_TEAM_ID = UUID.fromString("20000000-0000-0000-0000-000000000003");
    private static final UUID CORE_APP_SUPPORT_TEAM_ID = UUID.fromString("20000000-0000-0000-0000-000000000004");
    private static final UUID WEB_APP_SUPPORT_LEAD_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");
    private static final UUID WEB_APP_SUPPORT_MEMBER_ID = UUID.fromString("40000000-0000-0000-0000-000000000003");

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
                        .with(jwtWithRoles(UUID.randomUUID(), "CUSTOMER")))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAgentRoleOnCustomerTicketEndpoint() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .with(jwtWithRoles(UUID.randomUUID(), "AGENT")))
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
                DEFAULT_TOPIC_CODE,
                "Cannot open invoice",
                "Invoice page returns a blank screen after login.",
                TicketPriority.MEDIUM);

        mockMvc.perform(post("/api/tickets")
                        .header("X-Actor-Id", spoofedActorId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwtWithRoles(customerId, "CUSTOMER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()));
    }

    @Test
    void rejectsCrossCustomerTicketDetailWithJwt() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/api/tickets/{id}", ticketId)
                        .with(jwtWithRoles(otherCustomerId, "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void rejectsCrossCustomerCommentAccessWithJwt() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/api/tickets/{id}/comments", ticketId)
                        .with(jwtWithRoles(otherCustomerId, "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/tickets/{id}/comments/external", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddExternalCommentRequest("Cross access attempt.")))
                        .with(jwtWithRoles(otherCustomerId, "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/tickets/{id}/comments/read-state", ticketId)
                        .with(jwtWithRoles(otherCustomerId, "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/tickets/{id}/comments/read", ticketId)
                        .with(jwtWithRoles(otherCustomerId, "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void customerCanAccessOwnTicketAttachments() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(customerId);

        mockMvc.perform(get("/internal/tickets/{id}/attachment-access", ticketId)
                        .with(jwtWithRoles(customerId, "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.actorId").value(customerId.toString()))
                .andExpect(jsonPath("$.uploadAllowed").value(true))
                .andExpect(jsonPath("$.downloadAllowed").value(true));
    }

    @Test
    void customerCannotAccessOtherTicketAttachments() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID otherCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/internal/tickets/{id}/attachment-access", ticketId)
                        .with(jwtWithRoles(otherCustomerId, "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void adminCanAccessAnyTicketAttachments() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/internal/tickets/{id}/attachment-access", ticketId)
                        .with(jwtWithRoles(adminId, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.actorId").value(adminId.toString()));
    }

    @Test
    void assignedAgentCanAccessTicketAttachments() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID agentId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        assignTicketTo(ticketId, agentId, null, adminId);

        mockMvc.perform(get("/internal/tickets/{id}/attachment-access", ticketId)
                        .with(jwtWithRoles(agentId, "AGENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.actorId").value(agentId.toString()))
                .andExpect(jsonPath("$.uploadAllowed").value(true))
                .andExpect(jsonPath("$.downloadAllowed").value(true));
    }

    @Test
    void assignedTeamMemberCanAccessTicketAttachments() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/internal/tickets/{id}/attachment-access", ticketId)
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketId").value(ticketId.toString()))
                .andExpect(jsonPath("$.actorId").value(WEB_APP_SUPPORT_MEMBER_ID.toString()));
    }

    @Test
    void jwtTeamClaimDoesNotGrantTicketAccessWithoutDbMembership() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID spoofingAgentId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/api/agent/tickets/{id}", ticketId)
                        .with(jwtWithRolesAndTeams(spoofingAgentId, List.of(WEB_APP_SUPPORT_TEAM_ID), "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(get("/internal/tickets/{id}/attachment-access", ticketId)
                        .with(jwtWithRolesAndTeams(spoofingAgentId, List.of(WEB_APP_SUPPORT_TEAM_ID), "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void activeTeamMemberCanReadTeamTicketWithoutTeamClaim() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/api/agent/tickets")
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ticketId)).isNotEmpty());
    }

    @Test
    void teamMemberCannotManageTeamTicketWhenNotAssignedOrLead() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void teamMemberCanSelfAssignOwnTeamTicketThenManageIt() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(patch("/api/agent/tickets/{id}/assignment", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignTicketRequest(
                                WEB_APP_SUPPORT_MEMBER_ID,
                                WEB_APP_SUPPORT_TEAM_ID)))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(WEB_APP_SUPPORT_MEMBER_ID.toString()))
                .andExpect(jsonPath("$.assignedTeamId").value(WEB_APP_SUPPORT_TEAM_ID.toString()));

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TicketStatus.IN_PROGRESS.name()));

        mockMvc.perform(post("/api/agent/tickets/{id}/comments/external", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddExternalCommentRequest(
                                "We are checking the payment failure.")))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.visibility").value("EXTERNAL"));

        mockMvc.perform(post("/api/agent/tickets/{id}/worklogs", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddWorklogRequest(
                                LocalDate.parse("2026-06-06"),
                                30,
                                "Checked customer payment logs.")))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationMinutes").value(30));
    }

    @Test
    void teamMemberCannotSelfAssignOtherTeamTicket() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId, CORE_TOPIC_CODE);

        mockMvc.perform(patch("/api/agent/tickets/{id}/assignment", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AssignTicketRequest(
                                WEB_APP_SUPPORT_MEMBER_ID,
                                CORE_APP_SUPPORT_TEAM_ID)))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_MEMBER_ID, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void teamLeadCanManageOwnTeamTicket() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_LEAD_ID, "TEAM_LEAD")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(TicketStatus.IN_PROGRESS.name()));
    }

    @Test
    void teamLeadCannotManageOtherTeamTicket() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId, CORE_TOPIC_CODE);

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
                        .with(jwtWithRoles(WEB_APP_SUPPORT_LEAD_ID, "TEAM_LEAD")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void managerCanReadAllTicketsButCannotManage() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        mockMvc.perform(get("/api/agent/tickets")
                        .with(jwtWithRoles(managerId, "MANAGER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(ticketId)).isNotEmpty());

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
                        .with(jwtWithRoles(managerId, "MANAGER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void rejectsCustomerRoleOnAgentTicketEndpoints() throws Exception {
        mockMvc.perform(get("/api/agent/tickets")
                        .with(jwtWithRoles(UUID.randomUUID(), "CUSTOMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    void rejectsUnassignedAgentAccessToAssignedTicketWithJwt() throws Exception {
        UUID ownerCustomerId = UUID.randomUUID();
        UUID assignedAgentId = UUID.randomUUID();
        UUID otherAgentId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID ticketId = createTicketFor(ownerCustomerId);

        assignTicketTo(ticketId, assignedAgentId, null, adminId);

        mockMvc.perform(get("/api/agent/tickets/{id}", ticketId)
                        .with(jwtWithRoles(otherAgentId, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/agent/tickets/{id}/comments/internal", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddInternalNoteRequest("Cross access note.")))
                        .with(jwtWithRoles(otherAgentId, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/agent/tickets/{id}/comments/read-state", ticketId)
                        .with(jwtWithRoles(otherAgentId, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/agent/tickets/{id}/comments/read", ticketId)
                        .with(jwtWithRoles(otherAgentId, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        mockMvc.perform(patch("/api/agent/tickets/{id}/status", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ChangeTicketStatusRequest(TicketStatus.IN_PROGRESS)))
                        .with(jwtWithRoles(otherAgentId, "AGENT")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    private UUID createTicketFor(UUID customerId) throws Exception {
        return createTicketFor(customerId, DEFAULT_TOPIC_CODE);
    }

    private UUID createTicketFor(UUID customerId, String topicCode) throws Exception {
        UUID productId = productRepository.findByActiveTrueOrderByNameAsc().getFirst().getId();
        CreateTicketRequest request = new CreateTicketRequest(
                productId,
                topicCode,
                "Cannot download receipt",
                "Receipt download fails after payment confirmation.",
                TicketPriority.MEDIUM);

        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwtWithRoles(customerId, "CUSTOMER")))
                .andExpect(status().isCreated())
                .andReturn();

        String ticketId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        return UUID.fromString(ticketId);
    }

    private void assignTicketTo(UUID ticketId, UUID assigneeId, UUID teamId, UUID actorId) throws Exception {
        AssignTicketRequest request = new AssignTicketRequest(assigneeId, teamId);

        mockMvc.perform(patch("/api/agent/tickets/{id}/assignment", ticketId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwtWithRoles(actorId, "ADMIN")))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor jwtWithRoles(UUID subject, String... roles) {
        return jwt().jwt(token -> token
                .subject(subject.toString())
                .claim("realm_access", Map.of("roles", List.of(roles))));
    }

    private static RequestPostProcessor jwtWithRolesAndTeams(UUID subject, List<UUID> teamIds, String... roles) {
        return jwt().jwt(token -> token
                .subject(subject.toString())
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .claim("team_ids", teamIds.stream().map(UUID::toString).toList()));
    }
}
