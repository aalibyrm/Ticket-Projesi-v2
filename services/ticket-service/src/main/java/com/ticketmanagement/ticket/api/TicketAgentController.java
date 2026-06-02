package com.ticketmanagement.ticket.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.AddInternalNoteRequest;
import com.ticketmanagement.ticket.api.dto.AddWorklogRequest;
import com.ticketmanagement.ticket.api.dto.AssignTicketRequest;
import com.ticketmanagement.ticket.api.dto.ChangeTicketStatusRequest;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.api.dto.TicketWorklogResponse;
import com.ticketmanagement.ticket.application.AttachmentLookupContext;
import com.ticketmanagement.ticket.application.ForbiddenOperationException;
import com.ticketmanagement.ticket.application.SupportActorContext;
import com.ticketmanagement.ticket.application.TicketAgentCommandService;
import com.ticketmanagement.ticket.application.TicketAgentQueryService;

@RestController
@RequestMapping("/api/agent/tickets")
@RequiredArgsConstructor
class TicketAgentController {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String TEAM_IDS_CLAIM = "team_ids";
    private static final String AGENT_ROLE = "AGENT";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String LOCAL_DEFAULT_ROLE = AGENT_ROLE;

    private final TicketAgentCommandService ticketAgentCommandService;
    private final TicketAgentQueryService ticketAgentQueryService;

    // Agent'in yonetebilecegi ticket kuyrugunu dondurur.
    @GetMapping
    java.util.List<TicketResponse> listTickets(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ticketAgentQueryService.listTicketsForSupportActor(context);
    }

    // Agent'in yonetebilecegi tek bir ticket detayini dondurur.
    @GetMapping("/{id}")
    TicketResponse getTicket(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable UUID id) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ticketAgentQueryService.getTicketForSupportActor(
                context,
                id,
                new AttachmentLookupContext(resolveBearerToken(authorizationHeader)));
    }

    // Agent'in ticket uzerindeki internal ve external yorumlari okumasini saglar.
    @GetMapping("/{id}/comments")
    java.util.List<TicketCommentResponse> listComments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ticketAgentQueryService.listCommentsForSupportActor(context, id);
    }

    // Agent'in ticket uzerindeki worklog kayitlarini okumasini saglar.
    @GetMapping("/{id}/worklogs")
    java.util.List<TicketWorklogResponse> listWorklogs(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ticketAgentQueryService.listWorklogsForSupportActor(context, id);
    }

    // Agent'in ticket status bilgisini degistirmesini saglar.
    @PatchMapping("/{id}/status")
    TicketResponse changeStatus(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id,
            @Valid @RequestBody ChangeTicketStatusRequest request) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ticketAgentCommandService.changeStatus(context, id, request);
    }

    // Agent veya ekip atamasini ticket uzerinde gunceller.
    @PatchMapping("/{id}/assignment")
    TicketResponse assignTicket(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ticketAgentCommandService.assignTicket(context, id, request);
    }

    // Musterinin de gorebilecegi external yorumu ticket'a ekler.
    @PostMapping("/{id}/comments/external")
    ResponseEntity<TicketCommentResponse> addExternalComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id,
            @Valid @RequestBody AddExternalCommentRequest request) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketAgentCommandService.addExternalComment(context, id, request));
    }

    // Sadece support ekibinin gorecegi internal notu ticket'a ekler.
    @PostMapping("/{id}/comments/internal")
    ResponseEntity<TicketCommentResponse> addInternalNote(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id,
            @Valid @RequestBody AddInternalNoteRequest request) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketAgentCommandService.addInternalNote(context, id, request));
    }

    // Agent'in ticket uzerinde harcadigi sureyi worklog olarak kaydeder.
    @PostMapping("/{id}/worklogs")
    ResponseEntity<TicketWorklogResponse> addWorklog(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = "X-Actor-Team-Ids", required = false) String localTeamIds,
            @PathVariable UUID id,
            @Valid @RequestBody AddWorklogRequest request) {
        SupportActorContext context = resolveSupportActorContext(jwt, localActorId, localRoles, localTeamIds);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketAgentCommandService.addWorklog(context, id, request));
    }

    // Actor kimligi, rolleri ve ekiplerini support context icinde toplar.
    private SupportActorContext resolveSupportActorContext(
            Jwt jwt,
            UUID localActorId,
            String localRoles,
            String localTeamIds) {
        SupportActorContext context = new SupportActorContext(
                resolveSupportActorId(jwt, localActorId),
                resolveRoles(jwt, localRoles),
                resolveTeamIds(jwt, localTeamIds));
        ensureSupportRole(context);
        return context;
    }

    // JWT subject degerinden veya local test header'indan support actor kimligini cozer.
    private UUID resolveSupportActorId(Jwt jwt, UUID localActorId) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return parseActorSubject(jwt);
        }
        if (localActorId != null) {
            return localActorId;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }

    // JWT realm rollerini veya local test rollerini normalize eder.
    private Set<String> resolveRoles(Jwt jwt, String localRoles) {
        if (jwt != null) {
            Object realmAccess = jwt.getClaims().get(REALM_ACCESS_CLAIM);
            if (realmAccess instanceof Map<?, ?> access
                    && access.get(ROLES_CLAIM) instanceof Collection<?> roles) {
                return normalizeRoles(roles);
            }
            return Set.of();
        }
        if (localRoles == null || localRoles.isBlank()) {
            return Set.of(LOCAL_DEFAULT_ROLE);
        }
        return normalizeRoles(Arrays.asList(localRoles.split(",")));
    }

    // JWT team id claim'lerini veya local test team header'ini UUID setine cevirir.
    private Set<UUID> resolveTeamIds(Jwt jwt, String localTeamIds) {
        if (jwt != null) {
            Object teamIds = jwt.getClaims().get(TEAM_IDS_CLAIM);
            if (teamIds instanceof Collection<?> collection) {
                return parseTeamIds(collection);
            }
            return Set.of();
        }
        if (localTeamIds == null || localTeamIds.isBlank()) {
            return Set.of();
        }
        return parseTeamIds(Arrays.asList(localTeamIds.split(",")));
    }

    // Agent endpointlerini sadece AGENT veya ADMIN rolune sahip actor'lere acar.
    private void ensureSupportRole(SupportActorContext context) {
        if (context.hasRole(AGENT_ROLE) || context.hasRole(ADMIN_ROLE)) {
            return;
        }
        throw ForbiddenOperationException.accessDenied();
    }

    // Authorization header icinden ham bearer token degerini cikarir.
    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        String bearerPrefix = "Bearer ";
        if (!authorizationHeader.regionMatches(true, 0, bearerPrefix, 0, bearerPrefix.length())) {
            return null;
        }
        return authorizationHeader.substring(bearerPrefix.length()).trim();
    }

    // JWT subject degerini UUID agent kimligine cevirir.
    private UUID parseActorSubject(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user", exception);
        }
    }

    // Rol degerlerini karsilastirma icin buyuk harfli sete cevirir.
    private Set<String> normalizeRoles(java.util.Collection<?> roles) {
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .map(role -> role.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    // Team id degerlerini UUID olarak parse eder ve gecersiz degerleri yok sayar.
    private Set<UUID> parseTeamIds(Collection<?> teamIds) {
        return teamIds.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(String::trim)
                .filter(teamId -> !teamId.isBlank())
                .map(this::parseOptionalTeamId)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    // Tek bir team id degerini UUID olarak parse eder.
    private java.util.Optional<UUID> parseOptionalTeamId(String teamId) {
        try {
            return java.util.Optional.of(UUID.fromString(teamId));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }
}
