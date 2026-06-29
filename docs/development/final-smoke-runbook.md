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

3. Gercekci demo kullanici, ticket ve rapor verisini yukle:

```powershell
.\scripts\reset-local-demo-data.ps1 -ConfirmReset
```

4. Backend testlerinin yesil oldugunu dogrula:

```powershell
mvn -q test
```

5. Web uygulamasini baslat:

```powershell
Set-Location apps/web
npm install
npm run dev
```

6. Keycloak local demo kullanicilari:

| Kullanici | Gorunen ad | Rol | Departman | Ekip | Sifre |
| --- | --- | --- | --- | --- | --- |
| `customer.user` | Ayse Yilmaz | `CUSTOMER` | - | - | `Password123!` |
| `customer.mehmet` | Mehmet Demir | `CUSTOMER` | - | - | `Password123!` |
| `customer.zeynep` | Zeynep Kaya | `CUSTOMER` | - | - | `Password123!` |
| `customer.emre` | Emre Arslan | `CUSTOMER` | - | - | `Password123!` |
| `customer.ceren` | Ceren Aksoy | `CUSTOMER` | - | - | `Password123!` |
| `agent.identity` | Elif Aydin | `AGENT` | `ACCESS_MANAGEMENT` | `IDENTITY_OPERATIONS` | `Password123!` |
| `agent.permission` | Mert Kaya | `AGENT` | `ACCESS_MANAGEMENT` | `PERMISSION_OPERATIONS` | `Password123!` |
| `agent.web` | Deniz Arslan | `AGENT` | `APPLICATION_SUPPORT` | `WEB_APP_SUPPORT` | `Password123!` |
| `agent.core` | Selin Demir | `AGENT` | `APPLICATION_SUPPORT` | `CORE_APP_SUPPORT` | `Password123!` |
| `agent.network` | Baran Yilmaz | `AGENT` | `INFRASTRUCTURE` | `NETWORK_OPERATIONS` | `Password123!` |
| `agent.platform` | Ece Sahin | `AGENT` | `INFRASTRUCTURE` | `PLATFORM_OPERATIONS` | `Password123!` |
| `agent.billing` | Onur Demir | `AGENT` | `FINANCE_OPERATIONS` | `BILLING_OPERATIONS` | `Password123!` |
| `agent.payment` | Zeynep Ozturk | `AGENT` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS` | `Password123!` |
| `manager.user` | Deniz Karaca | `MANAGER` | - | - | `Password123!` |
| `admin.user` | Burak Ozkan | `ADMIN` | - | - | `Password123!` |

Topic routing'e gore `PAYMENT_FAILURE` ticket'lari `agent.payment`,
`WEB_PORTAL_BUG` ticket'lari `agent.web`, `CORE_SYSTEM_ERROR` ticket'lari
`agent.core` hesabinda gorunur.

7. Demo sirasi:

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
