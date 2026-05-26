package com.ticketmanagement.file.api;

import java.util.UUID;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import com.ticketmanagement.file.api.dto.UploadUrlResponse;
import com.ticketmanagement.file.application.FileMetadataResponse;
import com.ticketmanagement.file.application.FileUploadService;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
class FileUploadController {

    private final FileUploadService fileUploadService;

    // Client icin kisa sureli presigned upload URL uretir.
    @PostMapping("/uploads")
    UploadUrlResponse createUploadUrl(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @Valid @RequestBody CreateUploadUrlRequest request) {
        UUID actorId = resolveActorId(jwt, localActorId);
        return fileUploadService.createUploadUrl(actorId, request);
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

    // JWT subject degerini UUID actor kimligine cevirir.
    private UUID parseActorSubject(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user", exception);
        }
    }
}
