# Final Acceptance Evidence

Issue: #86
Run date: 2026-06-21

## Automated Verification

| Check | Result | Evidence |
| --- | --- | --- |
| Backend tests | PASS | `mvn -q test` completed successfully. |
| Frontend unit tests | PASS | `npm test`: 8 files, 13 tests passed. |
| Frontend production build | PASS | `npm run build` completed successfully. React Router v8 future flag warnings are non-blocking. |
| Mocked frontend e2e | PASS | `npm run e2e`: 1 smoke journey passed. |
| Full-stack acceptance e2e | PASS | `npm run e2e:fullstack`: 7 full-stack acceptance tests passed against real Keycloak, backend services, R2, Mailpit, and observability endpoints. |

## Full-Stack Scenario Status

| Scenario | Status |
| --- | --- |
| Role-based customer/agent access | PASS |
| Customer ticket creation with real attachment upload | PASS |
| Payment, web, and core topic routing | PASS |
| Attachment owner/routed-team/cross-team authorization | PASS |
| Customer-agent external messages and agent-only internal notes | PASS |
| Agent self-assignment, status transition, and worklog | PASS |
| Customer notification, Mailpit delivery, notification navigation, and SSE badge refresh | PASS |
| Manager report authorization | PASS |
| Swagger/API versioning and internal endpoint leak check | PASS |
| Validation and controlled authorization failures | PASS |

## Observability Evidence

| Area | Status | Evidence |
| --- | --- | --- |
| Prometheus | PASS | `http://localhost:9090/api/v1/targets` reported 6 active `ticket-backend-services` targets as `up` for ports 8081, 8082, 8083, 8084, 8085, and 8088. |
| Grafana | PASS | `http://localhost:3001/api/health` returned database `ok`, version `13.0.1`. |
| Jaeger | PASS | `http://localhost:16686/api/services` listed `api-gateway`, `ticket-service`, `notification-service`, `file-service`, `workflow-sla-service`, and `reporting-service`. |
| OpenSearch Dashboards | PASS | `http://localhost:5601/api/status` returned overall state `green`. |
| OpenSearch logs | PASS | `ticket-observability-*` indices were searchable through OpenSearch and contained 1072 documents. |
| Mailpit | PASS | `http://localhost:8025/api/v1/messages` showed ticket-created and status-changed messages, including support-recipient and customer-recipient mail. |

## Notes

- Local API Gateway rate-limit capacity is raised to 1000 only in the local profile so full-stack browser/SSE/API tests from the same loopback address do not fail with false `429` responses. Production remains configurable through `GATEWAY_RATE_LIMIT_CAPACITY`.
- OpenSearch HTTP API is protected with basic authentication. Evidence was gathered without printing credentials.
