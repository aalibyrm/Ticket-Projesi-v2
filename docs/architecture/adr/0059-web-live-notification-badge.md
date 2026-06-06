# ADR-0059: Web live notification badge

## Status
Accepted

## Context
Notification-service SSE stream'i zaten authenticated kullanici icin notification
eventlerini yayinliyor ve frontend bu eventlerde notification query'lerini invalidate
ediyor. Ust barda okunmamis bildirim sayisi gosterilmedigi icin kullanici yeni
bildirimi ancak `/notifications` sayfasina gidince fark ediyordu.

## Decision
Topbar bildirim butonu, mevcut `GET /api/notifications?read=false` sorgusundan
okunmamis bildirim sayisini alir ve MUI `Badge` ile gosterir. SSE event'i geldiginde
mevcut `NotificationLiveUpdates` invalidation mekanizmasi bu query'yi yeniler.

Ayri Redux notification store'u veya ikinci live transport eklenmedi.

## Consequences
- Canli bildirim gostergesi mevcut REST + TanStack Query cache modeliyle calisir.
- Read/unread state'in tek kaynagi notification-service olarak kalir.
- Ek WebSocket veya client-side event store karmasasi olusmaz.
- Badge sayisi query refresh gecikmesi kadar eventual consistent olur; bu lokal ve
  kurumsal dashboard kullanimi icin yeterlidir.
