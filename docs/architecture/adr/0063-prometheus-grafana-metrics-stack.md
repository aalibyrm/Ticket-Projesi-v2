# ADR-0063: Prometheus and Grafana metrics stack

## Status
Accepted

## Context
The project already has OpenTelemetry tracing with Jaeger and JSON log shipping
with Fluent Bit/OpenSearch. Metrics still need a dedicated local stack that fits
the current "backend services run as local JVM processes" development model.

## Decision
Use Spring Boot Actuator with Micrometer Prometheus registry in each backend
service. Prometheus scrapes `/actuator/prometheus`, and Grafana visualizes those
metrics through a provisioned Prometheus datasource and dashboard.

In local Docker Compose, Prometheus targets backend services through
`host.docker.internal:<port>` because the Spring Boot services are not currently
containerized.

## Consequences
- Metrics are handled by the standard Prometheus pull model instead of being
  mixed into the OpenTelemetry trace/log path.
- Grafana starts with a ready Prometheus datasource and backend metrics
  dashboard.
- `/actuator/prometheus` is public in local development so Prometheus can scrape
  it without JWT. Production deployments must restrict this endpoint with
  network policy, TLS, and service-account level access.
- If backend services later move into Compose/Kubernetes, Prometheus targets can
  change from `host.docker.internal` to service discovery without changing the
  application code.
