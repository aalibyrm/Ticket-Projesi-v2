package com.ticketmanagement.ticket.api;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.AddWorklogRequest;
import com.ticketmanagement.ticket.api.dto.AssignTicketRequest;
import com.ticketmanagement.ticket.api.dto.ChangeTicketStatusRequest;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.application.ForbiddenOperationException;
import com.ticketmanagement.ticket.application.TicketAgentCommandService;

@RestController
@RequestMapping("/api/agent/tickets")
@RequiredArgsConstructor
class TicketAgentController {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String AGENT_ROLE = "AGENT";
    private static final String ADMIN_ROLE = "ADMIN";

    private final TicketAgentCommandService ticketAgentCommandService;

    // Agent'in ticket status bilgisini degistirmesini saglar.
    @PatchMapping("/{id}/status")
    TicketResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id,
            @Valid @RequestBody ChangeTicketStatusRequest request) {
        UUID actorId = resolveSupportActorId(jwt, localActorId);
        return ticketAgentCommandService.changeStatus(actorId, id, request);
    }

    // Agent veya ekip atamasini ticket uzerinde gunceller.
    @PatchMapping("/{id}/assignment")
    TicketResponse assignTicket(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request) {
        UUID actorId = resolveSupportActorId(jwt, localActorId);
        return ticketAgentCommandService.assignTicket(actorId, id, request);
    }

    // Musterinin de gorebilecegi external yorumu ticket'a ekler.
    @PostMapping("/{id}/comments/external")
    ResponseEntity<TicketCommentResponse> addExternalComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id,
            @Valid @RequestBody AddExternalCommentRequest request) {
        UUID actorId = resolveSupportActorId(jwt, localActorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketAgentCommandService.addExternalComment(actorId, id, request));
    }

    // Agent'in ticket uzerinde harcadigi sureyi worklog olarak kaydeder.
    @PostMapping("/{id}/worklogs")
    ResponseEntity<TicketWorklogResponse> addWorklog(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id,
            @Valid @RequestBody AddWorklogRequest request) {
        UUID actorId = resolveSupportActorId(jwt, localActorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketAgentCommandService.addWorklog(actorId, id, request));
    }

    // JWT subject degerinden veya local test header'indan agent kimligini cozer.
    private UUID resolveSupportActorId(Jwt jwt, UUID localActorId) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            ensureSupportRole(jwt);
            return parseActorSubject(jwt);
        }
        if (localActorId != null) {
            return localActorId;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }

    // Agent endpointlerini sadece AGENT veya ADMIN rolune sahip JWT'lere acar.
    private void ensureSupportRole(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get(REALM_ACCESS_CLAIM);
        if (realmAccess instanceof Map<?, ?> access
                && access.get(ROLES_CLAIM) instanceof Collection<?> roles
                && (roles.contains(AGENT_ROLE) || roles.contains(ADMIN_ROLE))) {
            return;
        }
        throw ForbiddenOperationException.accessDenied();
    }

    // JWT subject degerini UUID agent kimligine cevirir.
    private UUID parseActorSubject(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user", exception);
        }
    }
}
