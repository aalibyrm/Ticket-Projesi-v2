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
docker compose --env-file .env -f infra/docker/docker-compose.yml config
docker compose --env-file .env -f infra/docker/docker-compose.yml up -d
docker compose --env-file .env -f infra/docker/docker-compose.yml ps
```

Servis URL'leri:

- Keycloak: `http://localhost:8080`
- Keycloak realm: `http://localhost:8080/realms/ticket-management`
- OpenSearch: `http://localhost:9200`
- OpenSearch Dashboards: `http://localhost:5601`
- Mailpit: `http://localhost:8025`
- Mailpit SMTP: `localhost:1025`
- Kafka external listener: `localhost:9094`
- OpenTelemetry OTLP gRPC: `localhost:4317`
- OpenTelemetry OTLP HTTP: `localhost:4318`

## Guvenlik Kurali

`.env`, private key, SMTP credential, R2 secret veya Keycloak admin secret
commit'lenmez.

`.env.example` sadece local development placeholder degerleri icerir. Prod veya
gercek provider secret'i bu dosyaya yazilmaz.

Notification e-posta gonderimi lokal profilde Mailpit SMTP'ye gider.
Gercek SMTP credential'lari `NOTIFICATION_SMTP_USERNAME` ve
`NOTIFICATION_SMTP_PASSWORD` gibi env/secret degerleriyle verilir; repo'ya
yazilmaz.
