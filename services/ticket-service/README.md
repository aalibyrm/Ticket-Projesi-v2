# ticket-service

Core ticket domain service. This service owns `ticket_schema`.

## Port

Default port: `8081`

## Database

Expected local datasource:

```properties
TICKET_DB_URL=jdbc:postgresql://localhost:5432/ticket_platform
TICKET_DB_USER=ticket_app
TICKET_DB_PASSWORD=ticket_dev_password
```

The PostgreSQL role must only have access to `ticket_schema`. Cross-service
schema access is intentionally not granted.

## Local Run

When running this service by itself, refresh the shared event contract first so
local Maven does not reuse an older snapshot jar:

```powershell
mvn -q -pl libs/event-contract install
mvn -pl services/ticket-service spring-boot:run -Dspring-boot.run.profiles=local
```

Health endpoint:

```text
GET http://localhost:8081/actuator/health
```

## Security

Default profile validates JWT tokens from:

```text
http://localhost:8080/realms/ticket-management
```

Realm roles under `realm_access.roles` are mapped to Spring authorities with the
`ROLE_` prefix.

The `local` profile disables JWT validation for early local development. In that
profile, `X-Actor-Id` is accepted as a temporary actor header. Production and
integration flows must use JWT subject as the actor identity.

## Internal Attachment Access

`GET /internal/tickets/{id}/attachment-access` is used by file-service before
issuing presigned upload or download URLs. The endpoint keeps ticket ownership
rules inside ticket-service:

- `CUSTOMER` can access only their own ticket attachments.
- `ADMIN` can access any ticket attachment.
- `MANAGER` can download ticket attachments read-only, but cannot upload.
- `AGENT` and `TEAM_LEAD` can access when they are the assigned agent or their
  active DB team membership includes the assigned team.

Support team membership is DB authoritative as of #63. `team_ids` JWT claims or
local `X-Actor-Team-Ids` headers are ignored for authorization. Active
`team_members` rows decide team read scope, and active `team_lead` rows decide
team lead management scope.

## Attachment Metadata Composition

Ticket detail responses include attachment metadata by calling file-service's
internal metadata endpoint after ticket-service authorizes the ticket owner. This
is API composition only: ticket-service does not own file metadata tables,
presigned URL generation, validation, object keys, or storage credentials.

## Organization Catalog

Sprint 10 organization routing modeli ticket-service icinde tutulur. #61 ile
aktif department, support team ve team member katalogu okunabilir hale geldi:

```text
GET /api/organization/departments
GET /api/organization/teams
GET /api/organization/teams/{teamId}/members
```

Seed edilen department seti:

- `ACCESS_MANAGEMENT`
- `APPLICATION_SUPPORT`
- `INFRASTRUCTURE`
- `FINANCE_OPERATIONS`

Her department altinda iki uzmanlik ekibi bulunur. Triage ayri bir ekip degil;
#62 ile deterministic `topic -> department -> team` routing rule olarak
uygulandi.

## Ticket Topic Routing

Customer ticket acarken assignment alani gondermez; `topicCode` gonderir.
Ticket-service aktif routing rule uzerinden default department ve team'i cozer.

```text
GET /api/ticket-topics
POST /api/tickets
```

Seed edilen topic routing seti:

- `PASSWORD_RESET` -> `ACCESS_MANAGEMENT` / `IDENTITY_OPERATIONS`
- `PERMISSION_REQUEST` -> `ACCESS_MANAGEMENT` / `PERMISSION_OPERATIONS`
- `WEB_PORTAL_BUG` -> `APPLICATION_SUPPORT` / `WEB_APP_SUPPORT`
- `CORE_SYSTEM_ERROR` -> `APPLICATION_SUPPORT` / `CORE_APP_SUPPORT`
- `NETWORK_CONNECTIVITY` -> `INFRASTRUCTURE` / `NETWORK_OPERATIONS`
- `SERVER_PLATFORM` -> `INFRASTRUCTURE` / `PLATFORM_OPERATIONS`
- `INVOICE_ISSUE` -> `FINANCE_OPERATIONS` / `BILLING_OPERATIONS`
- `PAYMENT_FAILURE` -> `FINANCE_OPERATIONS` / `PAYMENT_OPERATIONS`

Local Keycloak demo agent hesaplari team bazlidir ve `team_members.actor_id`
seed degerleriyle ayni user id'leri kullanir:

| Demo agent | Display name | Department | Team |
| --- | --- | --- | --- |
| `agent.identity` | Elif Aydin | `ACCESS_MANAGEMENT` | `IDENTITY_OPERATIONS` |
| `agent.permission` | Mert Kaya | `ACCESS_MANAGEMENT` | `PERMISSION_OPERATIONS` |
| `agent.web` | Deniz Arslan | `APPLICATION_SUPPORT` | `WEB_APP_SUPPORT` |
| `agent.core` | Selin Demir | `APPLICATION_SUPPORT` | `CORE_APP_SUPPORT` |
| `agent.network` | Baran Yilmaz | `INFRASTRUCTURE` | `NETWORK_OPERATIONS` |
| `agent.platform` | Ece Sahin | `INFRASTRUCTURE` | `PLATFORM_OPERATIONS` |
| `agent.billing` | Onur Demir | `FINANCE_OPERATIONS` | `BILLING_OPERATIONS` |
| `agent.payment` | Zeynep Ozturk | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS` |
