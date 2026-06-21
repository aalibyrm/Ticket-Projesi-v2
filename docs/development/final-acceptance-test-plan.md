# Final Acceptance Test Coverage

This runbook tracks issue #86: complete final acceptance test coverage.

## Decision Record

- Hybrid strategy: critical user flows run as automated full-stack Playwright/API smoke tests against real Keycloak, backend services, R2, and Mailpit. Dashboard-heavy observability checks stay manual because visual assertions against Grafana/OpenSearch Dashboards are brittle and add little value for this delivery.
- Test data strategy: tests create unique `E2E-<timestamp>` tickets and do not reset databases. This protects local demo data and avoids destructive setup.
- Gateway local profile uses a higher rate-limit capacity for acceptance runs. The full-stack suite opens several authenticated browser contexts and SSE streams from the same loopback address, so the production default can create false 429 failures even when business flows are healthy. Production remains configurable through `GATEWAY_RATE_LIMIT_CAPACITY`.
- Mobile acceptance remains out of scope for this issue, matching the current project plan.

## Automated Commands

Run these before delivery:

```powershell
mvn -q test
cd apps/web
npm test
npm run build
npm run e2e
npm run e2e:fullstack
```

`npm run e2e:fullstack` requires the real local stack:

- Frontend: `http://localhost:5173` (matches the Keycloak redirect URI used by the local realm)
- Keycloak: `http://localhost:8080`
- API Gateway: `http://localhost:8088`
- Mailpit: `http://localhost:8025`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001`
- Jaeger: `http://localhost:16686`
- OpenSearch Dashboards: `http://localhost:5601`
- File service must have real R2 `.env` values loaded.

## Manual Evidence Checklist

Mark each item as `PASS`, `FAIL`, or `BLOCKED` in the delivery notes. The latest evidence is tracked in
`docs/development/final-acceptance-evidence.md`.

| Area | Evidence | Status |
| --- | --- | --- |
| Prometheus | `http://localhost:9090/targets` shows gateway and services as UP. | PASS |
| Grafana | `http://localhost:3001` dashboard health is OK and dashboards are available for visual review. | PASS |
| Jaeger | `http://localhost:16686` lists gateway, ticket-service, and notification-service traces/services. | PASS |
| OpenSearch | `http://localhost:5601` is green and `ticket-observability-*` JSON logs are searchable. | PASS |
| Mailpit | `http://localhost:8025` shows ticket-created and status-changed mail. | PASS |

## Automated Scenario Report

`apps/web/e2e/fullstack/final-acceptance.spec.ts` covers:

- Role-based access gates for customer, agent, and manager routes.
- Customer ticket creation with real attachment upload.
- Topic routing to payment, web, and core teams.
- Attachment authorization for owner, routed team, and cross-team denial.
- Customer-agent external messages, agent internal note visibility, readable actor names.
- Agent assignment, status transition, and worklog.
- Customer notification, Mailpit delivery, notification click navigation, and SSE badge refresh.
- Swagger/API versioning and internal path leak checks.
- Validation and controlled authorization failures.
