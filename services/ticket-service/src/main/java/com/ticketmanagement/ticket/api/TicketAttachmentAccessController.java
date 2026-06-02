package com.ticketmanagement.ticket.api;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.ticket.api.dto.AttachmentAccessResponse;
import com.ticketmanagement.ticket.application.TicketAttachmentAccessService;

@RestController
@RequestMapping("/internal/tickets")
@RequiredArgsConstructor
class TicketAttachmentAccessController {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String LOCAL_DEFAULT_ROLE = "CUSTOMER";

    private final TicketAttachmentAccessService ticketAttachmentAccessService;

    // File-service icin ticket dosya erisim yetkisini dogrular.
    @GetMapping("/{id}/attachment-access")
    AttachmentAccessResponse checkAttachmentAccess(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @PathVariable UUID id) {
        UUID actorId = resolveActorId(jwt, localActorId);
        Set<String> roles = resolveRoles(jwt, localRoles);
        return ticketAttachmentAccessService.assertAttachmentAccess(id, actorId, roles);
    }

    // JWT subject degerinden veya local test header'indan actor kimligini cozer.
    private UUID resolveActorId(Jwt jwt, UUID localActorId) {
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
            if (realmAccess instanceof java.util.Map<?, ?> access
                    && access.get(ROLES_CLAIM) instanceof java.util.Collection<?> roles) {
                return normalizeRoles(roles);
            }
            return Set.of();
        }
        if (localRoles == null || localRoles.isBlank()) {
            return Set.of(LOCAL_DEFAULT_ROLE);
        }
        return normalizeRoles(Arrays.asList(localRoles.split(",")));
    }

    // JWT subject degerini UUID actor kimligine cevirir.
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
}
