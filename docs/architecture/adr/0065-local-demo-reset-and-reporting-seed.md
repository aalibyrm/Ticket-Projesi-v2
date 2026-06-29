# ADR-0065: Local Demo Reset and Realistic Reporting Seed

## Status

Accepted.

## Context

Final demo and manual testing need clean local ticket data, realistic user names,
and populated manager reporting panels. The data reset is destructive and must
not run automatically in shared or production-like environments.

## Decision

Use a guarded manual PowerShell script for local/demo reset and seed:

```powershell
.\scripts\reset-local-demo-data.ps1 -ConfirmReset
```

The script clears operational ticket traces, file metadata, notification/email
records, workflow SLA state, outbox records, and reporting projections. It keeps
consumer `processed_events` records so old Kafka messages cannot recreate old
demo data after service restart.

The script seeds both ticket-service operational tables and reporting-service
projection tables. Demo usernames remain stable, while display names and e-mail
profiles are updated to realistic fictitious users across Keycloak, backend
profile directories, notification recipients, and frontend fallbacks.

## Consequences

- Demo reset is explicit and cannot run as an automatic Flyway migration.
- No admin reset endpoint is exposed, so no extra public attack surface is
  introduced.
- Manager reports and customer/agent ticket screens show the same seeded data.
- Running Keycloak volumes need the script sync step or a realm volume reset to
  receive changed user display names.
