# Environment

Bu proje lokal gelistirmede `.env` dosyasini kullanir. `.env.example` sadece
template ve development placeholder degerlerini icerir.

## Dosya Kurali

```powershell
Copy-Item .env.example .env
```

- `.env` commit'lenmez.
- `.env.*` dosyalari commit'lenmez; yalnizca `.env.example` versiyonlanir.
- Private key, certificate, JKS, P12, SMTP password, R2 secret key ve gercek DB
  credential'lari repo'ya yazilmaz.

## Secret Alanlari

Asagidaki degiskenler lokal development disinda gercek secret kabul edilir:

- `POSTGRES_ADMIN_PASSWORD`
- `TICKET_DB_PASSWORD`
- `WORKFLOW_DB_PASSWORD`
- `FILE_DB_PASSWORD`
- `NOTIFICATION_DB_PASSWORD`
- `REPORTING_DB_PASSWORD`
- `KEYCLOAK_ADMIN_PASSWORD`
- `OPENSEARCH_INITIAL_ADMIN_PASSWORD`
- `NOTIFICATION_SMTP_USERNAME`
- `NOTIFICATION_SMTP_PASSWORD`
- `R2_ACCESS_KEY_ID`
- `R2_SECRET_ACCESS_KEY`

## Compose Profilleri

| Profil | Servisler | Amac |
| --- | --- | --- |
| `local` | PostgreSQL, Kafka, Keycloak, Mailpit | Minimum backend gelistirme |
| `dev` | `local` + OpenSearch, OpenSearch Dashboards, Jaeger, OTel Collector | Observability dahil gelistirme |
| `full` | `dev` ile ayni altyapi | Demo ve ileride app container'lari |

## Startup Order

1. `.env` hazirlanir.
2. `docker compose --profile local up -d` minimum altyapiyi baslatir.
3. Observability gerekiyorsa `--profile dev` kullanilir.
4. Backend servisleri altyapi hazir olduktan sonra lokal JVM process'i olarak
   baslatilir.
5. Web ve mobil istemciler gateway uzerinden API tuketir.

## Guvenlik

Development placeholder password'leri production veya paylasimli ortamda
kullanilmaz. Gercek ortamda secret'lar platform secret manager, CI secret store
veya deployment runtime tarafindan enjekte edilmelidir.
