# Final Smoke Runbook

Bu runbook #56 icin demo yolunu ve otomatik smoke kapsamlarini netlestirir.

## Otomatik Web Smoke

```powershell
Set-Location apps/web
npm install
npm run e2e
```

Testin kapsadigi yol:

1. Customer ekraninda ticket olusturma.
2. File-service presigned upload contract'i ile dosya ekleme ve complete akisi.
3. Agent workbench uzerinden ticket status guncelleme.
4. Agent external reply ve worklog kaydi.
5. Notification ekraninda customer-visible notification/email kuyruk sinyalini
   gorme.
6. Manager report ekraninda SLA ve agent performance panellerini gorme.

E2E auth sadece production olmayan dev/test bundle'inda explicit env flag veya
Playwright localStorage flag'i verildiginde calisir. Production build ve normal
local dev akisi Keycloak OIDC akisini kullanmaya devam eder.

## Full-Stack Manuel Demo

1. Repo kokunde `.env.example` dosyasini `.env` olarak kopyala.
2. Lokal altyapiyi baslat:

```powershell
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile local up -d
```

3. Backend testlerinin yesil oldugunu dogrula:

```powershell
mvn -q test
```

4. Web uygulamasini baslat:

```powershell
Set-Location apps/web
npm install
npm run dev
```

5. Keycloak local demo kullanicilari:

| Kullanici | Rol | Sifre |
| --- | --- | --- |
| `customer.user` | `CUSTOMER` | `Password123!` |
| `agent.user` | `AGENT` | `Password123!` |
| `manager.user` | `MANAGER` | `Password123!` |
| `admin.user` | `ADMIN` | `Password123!` |

6. Demo sirasi:

| Adim | Beklenen sonuc |
| --- | --- |
| Customer ticket olusturur | Ticket-service ticket'i topic routing ile team'e atar |
| Customer dosya ekler | File-service upload URL uretmeden once ticket-service internal authorization cagrisi yapar |
| Agent ticket'i acar | Agent sadece assigned team/agent kapsamindaki ticket'i yonetir |
| Agent status ve external comment ekler | Ticket eventleri outbox uzerinden yayinlanir |
| Notification kontrol edilir | Notification-service UI notification ve e-posta delivery kaydi olusturur |
| Mailpit `http://localhost:8025` acilir | Lokal e-posta template'i gorulur |
| Manager rapor ekranina girer | Reporting-service read modelinden SLA/status/agent metrikleri okunur |

## Kapsam Disi

Mobil E2E icin Detox/Appium kurulumu bu issue'da eklenmedi. Mobil kapsam #54 ve
#55 kararlarina uygun sekilde TypeScript build, Vitest ve temel ekran
implementasyonu olarak tutuldu. Native cihaz/emulator e2e daha sonra ayri issue
ile kararlastirilmelidir.
