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

## Guvenlik Kurali

`.env`, private key, SMTP credential, R2 secret veya Keycloak admin secret
commit'lenmez.

