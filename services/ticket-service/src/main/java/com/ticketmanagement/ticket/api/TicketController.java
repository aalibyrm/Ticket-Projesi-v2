package com.ticketmanagement.ticket.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ticketmanagement.ticket.api.dto.AddExternalCommentRequest;
import com.ticketmanagement.ticket.api.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.api.dto.TicketCommentResponse;
import com.ticketmanagement.ticket.api.dto.TicketResponse;
import com.ticketmanagement.ticket.application.AttachmentLookupContext;
import com.ticketmanagement.ticket.application.ForbiddenOperationException;
import com.ticketmanagement.ticket.application.TicketCommandService;
import com.ticketmanagement.ticket.application.TicketQueryService;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
class TicketController {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final TicketCommandService ticketCommandService;
    private final TicketQueryService ticketQueryService;

    // Musterinin yeni bir ticket acmasini saglar.
    @PostMapping
    ResponseEntity<TicketResponse> createTicket(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @Valid @RequestBody CreateTicketRequest request) {
        UUID customerId = resolveCustomerId(jwt, localActorId);
        TicketResponse response = ticketCommandService.createTicket(customerId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    // Musterinin kendi ticket listesini dondurur.
    @GetMapping
    List<TicketResponse> listOwnTickets(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId) {
        UUID customerId = resolveCustomerId(jwt, localActorId);
        return ticketQueryService.listTicketsForCustomer(customerId);
    }

    // Musterinin kendisine ait tek bir ticket detayini dondurur.
    @GetMapping("/{id}")
    TicketResponse getOwnTicket(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable UUID id) {
        UUID customerId = resolveCustomerId(jwt, localActorId);
        return ticketQueryService.getTicketForCustomer(customerId, id, new AttachmentLookupContext(
                resolveBearerToken(authorizationHeader)));
    }

    // Musterinin kendi ticket'indaki external yorumlari okumasini saglar.
    @GetMapping("/{id}/comments")
    List<TicketCommentResponse> listOwnTicketComments(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id) {
        UUID customerId = resolveCustomerId(jwt, localActorId);
        return ticketQueryService.listExternalCommentsForCustomer(customerId, id);
    }

    // Musterinin kendi ticket'ina external yorum eklemesini saglar.
    @PostMapping("/{id}/comments/external")
    ResponseEntity<TicketCommentResponse> addOwnTicketComment(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id,
            @Valid @RequestBody AddExternalCommentRequest request) {
        UUID customerId = resolveCustomerId(jwt, localActorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketCommandService.addCustomerExternalComment(customerId, id, request));
    }

    // JWT subject degerinden veya local test header'indan musteri kimligini cozer.
    private UUID resolveCustomerId(Jwt jwt, UUID localActorId) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            ensureCustomerRole(jwt);
            return parseCustomerSubject(jwt);
        }
        if (localActorId != null) {
            return localActorId;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
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

    // Customer endpointlerini sadece CUSTOMER rolune sahip JWT'lere acar.
    private void ensureCustomerRole(Jwt jwt) {
        Object realmAccess = jwt.getClaims().get(REALM_ACCESS_CLAIM);
        if (realmAccess instanceof java.util.Map<?, ?> access
                && access.get(ROLES_CLAIM) instanceof java.util.Collection<?> roles
                && roles.contains(CUSTOMER_ROLE)) {
            return;
        }
        throw ForbiddenOperationException.customerRoleRequired();
    }

    // JWT subject degerini UUID musteri kimligine cevirir.
    private UUID parseCustomerSubject(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user", exception);
        }
    }
}
