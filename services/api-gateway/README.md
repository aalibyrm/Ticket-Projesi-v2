# api-gateway

Spring Cloud Gateway tabanli web ve mobil API giris noktasi.

## Port

Default port: `8088`

## Route Placeholder'lari

- `/api/v1/tickets/**`, `/api/v1/products/**` -> `ticket-service`
- `/api/v1/files/**` -> `file-service`
- `/api/v1/workflows/**`, `/api/v1/sla/**` -> `workflow-sla-service`
- `/api/v1/notifications/**` -> `notification-service`
- `/api/v1/reports/**` -> `reporting-service`

Legacy `/api/**` route'lari migration penceresinde ayni authorization matrisiyle
korunarak calismaya devam eder.

## Swagger/OpenAPI

Gateway lokal Swagger UI'yi `/swagger-ui.html` uzerinden sunar ve servis
OpenAPI JSON'larini asagidaki path'lerden aggregate eder:

- `/v3/api-docs/ticket-service`
- `/v3/api-docs/file-service`
- `/v3/api-docs/workflow-sla-service`
- `/v3/api-docs/notification-service`
- `/v3/api-docs/reporting-service`

Servis spec'leri public kontrat olarak `/api/v1/**` path'lerini gosterir.
Production ortaminda gerekiyorsa `SPRINGDOC_API_DOCS_ENABLED=false` ve
`SPRINGDOC_SWAGGER_UI_ENABLED=false` ile dokuman endpointleri kapatilabilir.

## Guvenlik

Default profil JWT resource server davranisini acik kabul eder:

```properties
GATEWAY_JWT_ENABLED=true
KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/ticket-management
```

`local` profil sadece erken/lokal bootstrapping icin JWT kontrolunu kapatir. Bu
profil production icin kullanilmaz.

Keycloak realm rolleri `realm_access.roles` alanindan okunur ve Spring
Security'de `ROLE_` prefix'i ile authority'ye cevrilir.

Gateway route bazli ilk authorization filtresini uygular:

| Route | Roller |
| --- | --- |
| `/api/v1/tickets/**` | `CUSTOMER`, `ADMIN` |
| `/api/v1/agent/tickets/**` | `AGENT`, `ADMIN` |
| `/api/v1/workflows/**` | `AGENT`, `ADMIN` |
| `/api/v1/reports/**` | `MANAGER`, `ADMIN` |
| `/api/v1/sla/**` | `MANAGER`, `ADMIN` |
| `/api/v1/products/**` | `CUSTOMER`, `AGENT`, `MANAGER`, `ADMIN` |
| `/api/v1/files/**`, `/api/v1/notifications/**` | Authenticated user |

Servisler kendi domain authorization kontrollerini ayrica uygular; gateway tek
guvenlik siniri kabul edilmez.

## Edge Hardening

CORS allowlist explicit origin listesidir; credential kullandigimiz icin `*`
origin kabul edilmez.

```properties
GATEWAY_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

Rate limiter Redis gerektirmeyen in-memory fixed-window stratejisiyle baslar.
Bu lokal/demo ortaminda yeterlidir; gateway yatay olceklendiginde Redis/Bucket4j
veya platform limiter'a tasinmalidir. Client tarafindan spoof edilebilen
forwarding header'lari varsayilan rate limit kimligi olarak guvenilmez.

```properties
GATEWAY_RATE_LIMIT_ENABLED=true
GATEWAY_RATE_LIMIT_CAPACITY=120
GATEWAY_RATE_LIMIT_WINDOW=PT1M
```

Gateway tum response'lara temel guvenlik header'larini ekler: CSP, HSTS,
`X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy` ve
`Permissions-Policy`.

## Lokal Calistirma

```powershell
mvn -pl services/api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

Health endpoint:

```text
GET http://localhost:8088/actuator/health
```
