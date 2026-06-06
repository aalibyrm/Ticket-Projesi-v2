# Docker Compose

Bu dizin lokal gelistirme ve demo altyapisini barindirir.

## Bilesenler

- PostgreSQL
- Kafka
- Keycloak
- OpenSearch
- OpenSearch Dashboards
- OpenTelemetry Collector
- Jaeger
- Fluent Bit
- Prometheus
- Grafana
- Mailpit

## Profiller

| Profil | Kapsam | Ne zaman kullanilir |
| --- | --- | --- |
| `local` | PostgreSQL, Kafka, Keycloak, Mailpit | Backend servislerini lokal JVM ile gelistirirken minimum altyapi |
| `dev` | `local` + OpenSearch, OpenSearch Dashboards, Jaeger, OTel Collector, Fluent Bit, Prometheus, Grafana | Observability dahil gunluk gelistirme |
| `full` | `dev` ile ayni altyapi; ileride app container'lari icin ayrildi | Demo veya uctan uca ortam |

## Komutlar

```powershell
docker compose --env-file ../../.env -f docker-compose.yml --profile local config
docker compose --env-file ../../.env -f docker-compose.yml --profile local up -d
docker compose --env-file ../../.env -f docker-compose.yml --profile local ps
docker compose --env-file ../../.env -f docker-compose.yml --profile local down
```

Repo kokunden calistirma:

```powershell
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile dev up -d
```

## Baslatma Sirasi

Compose container siralamasini `depends_on` ve healthcheck'ler ile yonetir.

1. `postgres`, `kafka`, `keycloak`, `mailpit` core profilde baslar.
2. `opensearch` saglikli olunca `opensearch-dashboards`, `otel-collector` ve
   `fluent-bit` baslar.
3. `otel-collector`, `jaeger` container'i baslamadan calistirilmaz.
4. `prometheus` lokal JVM servislerini `host.docker.internal` uzerinden scrape
   eder; `grafana` Prometheus datasource ile baslar.
5. Backend servisleri container degil lokal JVM process'i olarak baslatilir ve
   hazir altyapiya baglanir.
6. Backend servisleri repo kokunden calistirildiginda `logs/*.json.log`
   dosyalari uretilir; `fluent-bit` bu dosyalari OpenSearch'e aktarir.

## Notlar

- Bu Compose dosyasi lokal gelistirme icindir.
- Production secret'lari commit'lenmez.
- `.env.example` sadece development placeholder degerleri icerir.
- Gercek SMTP, R2, Keycloak admin veya DB credential'lari `.env` ya da secret
  manager uzerinden verilir.
