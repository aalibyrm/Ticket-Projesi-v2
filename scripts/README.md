# Scripts

Tekrarlanabilir gelistirme ve bakim scriptleri bu dizinde tutulur.

Scriptler destructive olmamali ve calismadan once ne yaptigini acikca
belirtmelidir.

## start-observability-services.ps1

Tum Java backend servislerini OpenTelemetry Java Agent ile baslatir. Jaeger'da
servislerin eksik gorunmesini engellemek icin her servis dogru
`OTEL_SERVICE_NAME` degeriyle ayri JVM process'i olarak calistirilir.

```powershell
.\scripts\start-observability-services.ps1 -Restart
```

## start-ticket-service-local.ps1

Sadece `ticket-service` icin lokal restart yapar. Script once `.env` dosyasini
process ortam degiskenlerine yukler, `TICKET_DB_URL`, `TICKET_DB_USER` ve
`TICKET_DB_PASSWORD` degerlerini dogrular, sonra servisi baslatir.

Karar: tek servis restart icin raw `mvn spring-boot:run` yerine bu script
kullanilir. Nedeni, PowerShell oturumunda `.env` yuklu degilse Spring Boot
`application.yml` icindeki varsayilan `localhost:5432` degerine duser ve Docker
PostgreSQL portu olan `55432` yerine yanlis hedefe baglanmaya calisir. Script
secret degerlerini ekrana basmaz; sadece yuklenen key sayisini yazar.

```powershell
.\scripts\start-ticket-service-local.ps1 -Restart
```

Loglar `logs/ticket-service.local.out` ve `logs/ticket-service.local.err`
dosyalarina yazilir.

## reset-local-demo-data.ps1

Local/demo PostgreSQL verisini temizler ve gercekci demo ticket, agent,
customer ve reporting projection verisi uretir. Script destructive oldugu icin
`-ConfirmReset` olmadan calismaz.

Temizlenen alanlar: ticket, comment, worklog, conversation read state,
attachment metadata, notification, email delivery, workflow SLA state, outbox ve
reporting projection kayitlari. Consumer `processed_events` kayitlari korunur;
boylece eski Kafka eventleri servis restart sonrasi eski demo verisini yeniden
uretemez.

Keycloak calisiyorsa kullanici ad-soyad/e-posta bilgileri Admin REST uzerinden
senkronlanir. Mailpit calisiyorsa eski mail kutusu temizlenir.

Yeni fixed-ID demo kullanicilari calisan realm icinde eksikse script bu
kullanicilari sonradan olusturmaz. Keycloak Admin REST yeni kullanici
olustururken `sub` degerini garanti etmedigi icin eksik kullanici durumunda
local Keycloak container'ini realm export'tan yeniden import etmek gerekir.

```powershell
.\scripts\reset-local-demo-data.ps1 -ConfirmReset
```
