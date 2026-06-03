# Local Setup

Bu proje Docker Compose tabanli lokal gelistirme akisiyle baslayacak.

## On Gereksinimler

- Java 21
- Docker Desktop
- Node.js LTS
- GitHub CLI
- Cloudflare R2 credential'lari sadece lokal secret olarak

## Calistirma Yaklasimi

1. `.env.example` dosyasi kopyalanir.
2. Secret degerler `.env` icinde lokal olarak tanimlanir.
3. `infra/docker` altindaki Compose profilleri ile altyapi ayaga kaldirilir.
4. Backend servisleri local profile ile calistirilir.
5. Web ve mobil istemciler gateway uzerinden API tuketir.

## Compose Komutlari

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile local config
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile local up -d
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile local ps
```

Profil secimi:

- `local`: PostgreSQL, Kafka, Keycloak ve Mailpit.
- `dev`: `local` + OpenSearch, OpenSearch Dashboards, Jaeger ve OTel
  Collector.
- `full`: `dev` ile ayni altyapi; ileride app container'lari eklendiginde
  uctan uca demo profili olarak genisletilecek.

Observability dahil gelistirme icin:

```powershell
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile dev up -d
```

Servis URL'leri:

- Keycloak: `http://localhost:8080`
- Keycloak realm: `http://localhost:8080/realms/ticket-management`
- OpenSearch: `http://localhost:9200`
- OpenSearch Dashboards: `http://localhost:5601`
- Jaeger: `http://localhost:16686`
- Mailpit: `http://localhost:8025`
- Mailpit SMTP: `localhost:1025`
- Kafka external listener: `localhost:9094`
- Reporting-service: `http://localhost:8085`
- OpenTelemetry OTLP gRPC: `localhost:4317`
- OpenTelemetry OTLP HTTP: `localhost:4318`

Mobil istemci:

```powershell
Set-Location apps/mobile
Copy-Item .env.example .env
npm install
npm run start
```

Android emulator gateway'e host makine uzerinden erisecekse
`EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080` kullanin. Fiziksel cihazda
host makinenin LAN IP adresi kullanilmalidir.

Final smoke ve demo akisi icin `docs/development/final-smoke-runbook.md`
dosyasini takip edin.

Trace kurulumu ve servislerin OpenTelemetry Java Agent ile calistirilmasi icin
`docs/development/observability.md` dosyasini takip edin.

Environment degiskenleri ve secret kurallari icin
`docs/development/environment.md` dosyasini takip edin.

## Guvenlik Kurali

`.env`, private key, SMTP credential, R2 secret veya Keycloak admin secret
commit'lenmez.

`.env.example` sadece local development placeholder degerleri icerir. Prod veya
gercek provider secret'i bu dosyaya yazilmaz.

Notification e-posta gonderimi lokal profilde Mailpit SMTP'ye gider.
Gercek SMTP credential'lari `NOTIFICATION_SMTP_USERNAME` ve
`NOTIFICATION_SMTP_PASSWORD` gibi env/secret degerleriyle verilir; repo'ya
yazilmaz.
