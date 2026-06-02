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

```powershell
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
- `AGENT` and assigned team access stays closed until the assignment model is
  introduced.

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
#62 kapsaminda deterministic `topic -> department -> team` routing rule olarak
uygulanacaktir.
