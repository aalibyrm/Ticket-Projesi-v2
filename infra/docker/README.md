# Docker Compose

Bu dizin lokal gelistirme ve demo altyapisini barindirir.

## Bilesenler

- PostgreSQL
- Kafka
- Keycloak
- OpenSearch
- OpenSearch Dashboards
- OpenTelemetry Collector
- Mailpit

## Komutlar

```powershell
docker compose --env-file ../../.env -f docker-compose.yml config
docker compose --env-file ../../.env -f docker-compose.yml up -d
docker compose --env-file ../../.env -f docker-compose.yml down
```

Repo kokunden calistirma:

```powershell
docker compose --env-file .env -f infra/docker/docker-compose.yml up -d
```

## Notlar

- Bu Compose dosyasi lokal gelistirme icindir.
- Production secret'lari commit'lenmez.
- Keycloak realm import'u ayri issue kapsaminda eklenecektir.

