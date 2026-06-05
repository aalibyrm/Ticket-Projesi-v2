# ADR-0052: SSE Live Updates

## Status

Accepted

## Context

Ticket conversation, unread state and UI notification screens need live updates
without forcing users to refresh pages. The project already uses REST for
commands and queries, Kafka/outbox for backend events, and TanStack Query for
frontend server-state caching.

The evaluated options were:

- Polling: simplest, but creates repeated REST traffic and only delayed updates.
- Server-Sent Events: one-way server-to-client stream over HTTP.
- WebSocket/STOMP: bidirectional live channel with higher operational and
authorization complexity.

## Decision

Use Server-Sent Events for live updates.

The user selected option B. We keep REST as the authoritative write/read path and
use SSE only as a lightweight invalidation signal. The first implementation
places the stream in `notification-service` at `/api/notifications/stream`.
Frontend clients open an authenticated fetch-based SSE stream and invalidate
TanStack Query caches when `notification.created` or `notification.read` events
arrive.

Native browser `EventSource` is not used because it cannot attach the Keycloak
Bearer token header. The frontend still consumes SSE format, but opens the
stream with `fetch` so the existing Authorization header model remains intact.

## Rationale

SSE fits the ticket management use case because the live direction is mostly
server-to-client: new message, unread state changed, notification created, SLA
warning. Comments and read markers stay as REST calls, so existing domain
authorization remains in ticket-service and notification-service.

Compared with polling, SSE reduces repeated requests and gives faster UI
feedback. Compared with WebSocket/STOMP, it avoids bidirectional channel
authorization, broker relay setup and presence/typing complexity that the
current scope does not need.

## Consequences

The first version uses an in-process emitter registry in notification-service.
This is sufficient for local/demo and single-instance deployment. If
notification-service is horizontally scaled, live fanout must be promoted to a
shared pub/sub layer, such as Redis pub/sub or a per-instance Kafka live-events
consumer strategy.

SSE payloads do not carry ticket comment bodies. They carry notification metadata
only; frontend re-fetches the authorized REST resources after receiving the
event. This prevents accidental sensitive comment leakage and keeps ticket
access control in the owning services.
