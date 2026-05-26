package com.ticketmanagement.file.api;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.file.api.dto.CreateUploadUrlRequest;
import com.ticketmanagement.file.api.dto.DownloadUrlResponse;
import com.ticketmanagement.file.api.dto.UploadUrlResponse;
import com.ticketmanagement.file.application.FileMetadataResponse;
import com.ticketmanagement.file.application.FileUploadService;
import com.ticketmanagement.file.application.ticket.TicketAccessContext;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
class FileUploadController {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String LOCAL_DEFAULT_ROLE = "CUSTOMER";

    private final FileUploadService fileUploadService;

    // Client icin kisa sureli presigned upload URL uretir.
    @PostMapping("/uploads")
    UploadUrlResponse createUploadUrl(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody CreateUploadUrlRequest request) {
        TicketAccessContext context = resolveAccessContext(jwt, localActorId, localRoles, authorizationHeader);
        return fileUploadService.createUploadUrl(context, request);
    }

    // Client upload tamamlandiginda metadata kaydini tamamlar.
    @PostMapping("/uploads/{id}/complete")
    FileMetadataResponse completeUpload(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id) {
        UUID actorId = resolveActorId(jwt, localActorId);
        return fileUploadService.completeUpload(actorId, id);
    }

    // Client icin kisa sureli presigned download URL uretir.
    @PostMapping("/{id}/download-url")
    DownloadUrlResponse createDownloadUrl(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestHeader(value = "X-Actor-Roles", required = false) String localRoles,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @PathVariable UUID id) {
        TicketAccessContext context = resolveAccessContext(jwt, localActorId, localRoles, authorizationHeader);
        return fileUploadService.createDownloadUrl(id, context);
    }

    // Actor kimligi, rolleri ve bearer token bilgisini tek context icinde toplar.
    private TicketAccessContext resolveAccessContext(
            Jwt jwt,
            UUID localActorId,
            String localRoles,
            String authorizationHeader) {
        return new TicketAccessContext(
                resolveActorId(jwt, localActorId),
                resolveRoles(jwt, localRoles),
                resolveBearerToken(authorizationHeader));
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
