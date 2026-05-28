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
- [Observability](docs/development/observability.md)
- [Service Conventions](docs/development/service-conventions.md)
- [Backend Build](docs/development/backend-build.md)

## Lokal Altyapi

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f infra/docker/docker-compose.yml config
docker compose --env-file .env -f infra/docker/docker-compose.yml up -d
```

`.env` dosyasi lokal secret kabul edilir ve commit'lenmez.

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
  observability/   OpenTelemetry, OpenSearch ve dashboard konfigurasyonu
docs/
  architecture/
  api/
  development/
scripts/
```
