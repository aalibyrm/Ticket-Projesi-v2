# ADR-0057: Display Name Profile Resolution

## Status

Accepted

## Context

Agent UI'da customer ve assignee alanlari raw UUID olarak gorunuyordu. Bu,
operasyon ekraninda okunabilirligi dusuruyor. Ticket-service su an identity
verisinin sahibi degil; Keycloak kimlik dogrulama otoritesi, ticket-service ise
ticket ve organization routing sahibidir.

## Decision

Bu asamada ticket-service icine yalnizca demo actor id'leri icin lightweight
profile directory eklenir. Organization API team member response'lari
`displayName` ve `email` alanlarini dondurur. Frontend, mevcut oturum
kullanicisi ve bilinen demo profil kataloguyla UUID'leri okunabilir isimlere
cevirir; profil bulunamazsa tam UUID yerine kisa fallback gosterir.

## Consequences

- Assignment dropdown ve ticket detail alanlari UUID yerine isim odakli olur.
- Ticket-service henuz genel purpose identity/profile service'e donusmez.
- Gercek multi-tenant user profile ihtiyaci buyurse, bu lookup ayrica
  identity/profile service veya Keycloak admin-backed adapter'a tasinabilir.
