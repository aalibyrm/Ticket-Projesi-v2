# api-gateway

Spring Cloud Gateway tabanli web ve mobil API giris noktasi.

## Port

Default port: `8088`

## Route Placeholder'lari

- `/api/tickets/**`, `/api/products/**` -> `ticket-service`
- `/api/files/**` -> `file-service`
- `/api/workflows/**`, `/api/sla/**` -> `workflow-sla-service`
- `/api/notifications/**` -> `notification-service`
- `/api/reports/**` -> `reporting-service`

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

## Lokal Calistirma

```powershell
mvn -pl services/api-gateway spring-boot:run -Dspring-boot.run.profiles=local
```

Health endpoint:

```text
GET http://localhost:8088/actuator/health
```
