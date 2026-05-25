# Ticket Projesi v2

Prod'a yakin tasarlanan IT Service Management / Ticket Management sistemi.

Bu repo, Java 21 ve Spring Boot 3.x mikroservisleri, React web istemcisi, temel
React Native mobil istemcisi, PostgreSQL, Keycloak, Kafka, Cloudflare R2,
OpenTelemetry, OpenSearch ve gercek e-posta gonderimi uzerine kurulacaktir.

## Baslangic Kararlari

- Mimari kararlar `docs/architecture/adr` altinda ADR olarak tutulur.
- Her is GitHub issue ile takip edilir.
- Her uygulama commit mesajinda ilgili issue numarasi bulunur: `#<issueId>`.
- Fazlar GitHub milestone olarak sprintlere bolunmustur.

## Dokumanlar

- [Architecture Decision Log](docs/architecture/decision-log.md)
- [Sprint Plan](docs/architecture/sprint-plan.md)
- [Commit Policy](docs/architecture/commit-policy.md)
- [Local Setup](docs/development/local-setup.md)
- [Service Conventions](docs/development/service-conventions.md)

## Monorepo Dizini

```text
apps/
  mobile/          React Native mobil istemci
  web/             React web istemci
services/
  api-gateway/
  ticket-service/
  workflow-sla-service/
  file-service/
  notification-service/
  reporting-service/
infra/
  docker/          Compose ve container yardimci dosyalari
  keycloak/        Realm export ve seed kullanicilar
  observability/   OpenTelemetry ve OpenSearch konfigurasyonu
docs/
  architecture/
  api/
  development/
scripts/
```
