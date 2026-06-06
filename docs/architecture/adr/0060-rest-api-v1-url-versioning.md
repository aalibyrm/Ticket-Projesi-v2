# ADR-0060: REST API v1 URL versioning

## Status
Accepted

## Context
Public web and mobile clients were calling unversioned `/api/**` gateway routes.
The project now needs an explicit REST API versioning strategy before Swagger
generation and frontend API client generation become broader concerns.

## Decision
Public API routes use URL versioning with `/api/v1/**`.

The gateway owns the public version prefix and rewrites `/api/v1/**` to the
current service-local `/api/**` paths. Service controllers keep their current
paths for this phase so each microservice's internal API boundary is not churned
just to introduce the first public version.

Legacy `/api/**` gateway routes remain protected and available temporarily during
the migration window. New frontend calls use `/api/v1/**`.

## Consequences
- Public contracts are explicit and friendly to Swagger/OpenAPI and generated
  clients.
- Gateway security rules protect both versioned and legacy paths with the same
  role matrix.
- Services avoid a broad controller rename in this phase.
- A future v2 can be introduced at the gateway as `/api/v2/**`, then routed to
  either new service endpoints or dedicated compatibility adapters.
