package com.ticketmanagement.notification.api;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ticketmanagement.notification.api.dto.NotificationResponse;
import com.ticketmanagement.notification.application.NotificationQueryService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
class NotificationController {

    private final NotificationQueryService notificationQueryService;

    // Kullaniciya ait notification listesini opsiyonel read filtresiyle dondurur.
    @GetMapping
    List<NotificationResponse> listNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @RequestParam(required = false) Boolean read) {
        UUID userId = resolveUserId(jwt, localActorId);
        return notificationQueryService.listUserNotifications(userId, read);
    }

    // Kullaniciya ait tek bir notification kaydini okundu olarak isaretler.
    @PatchMapping("/{id}/read")
    NotificationResponse markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-Actor-Id", required = false) UUID localActorId,
            @PathVariable UUID id) {
        UUID userId = resolveUserId(jwt, localActorId);
        return notificationQueryService.markAsRead(userId, id);
    }

    // JWT subject degerinden veya local test header'indan kullanici kimligini cozer.
    private UUID resolveUserId(Jwt jwt, UUID localActorId) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return parseSubject(jwt);
        }
        if (localActorId != null) {
            return localActorId;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }

    // JWT subject degerini UUID kullanici kimligine cevirir.
    private UUID parseSubject(Jwt jwt) {
        try {
            return UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authenticated user", exception);
        }
    }
}
