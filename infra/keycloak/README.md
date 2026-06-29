# Keycloak

This directory contains the local Keycloak realm export for the Ticket
Management platform.

## Realm

- Realm name: `ticket-management`
- Roles: `CUSTOMER`, `AGENT`, `MANAGER`, `ADMIN`
- Remember me: enabled
- Brute force protection: enabled
- OTP policy: TOTP configured at realm level
- Login theme: `ticket`

## Clients

- `ticket-web`: public OIDC client for React web
- `ticket-mobile`: public OIDC client for React Native
- `ticket-dev-cli`: local-only public client for automated token checks
- `api-gateway`: confidential local service client
- `ticket-service`: confidential local service client
- `workflow-sla-service`: confidential local service client
- `file-service`: confidential local service client
- `notification-service`: confidential local service client
- `reporting-service`: confidential local service client

## Demo Users

| Username | Display name | Email | Role | Department | Team | Password |
| --- | --- | --- | --- | --- | --- | --- |
| `customer.user` | Ayse Yilmaz | `ayse.yilmaz@example.local` | `CUSTOMER` | - | - | `Password123!` |
| `customer.mehmet` | Mehmet Demir | `mehmet.demir@example.local` | `CUSTOMER` | - | - | `Password123!` |
| `customer.zeynep` | Zeynep Kaya | `zeynep.kaya@example.local` | `CUSTOMER` | - | - | `Password123!` |
| `customer.emre` | Emre Arslan | `emre.arslan@example.local` | `CUSTOMER` | - | - | `Password123!` |
| `customer.ceren` | Ceren Aksoy | `ceren.aksoy@example.local` | `CUSTOMER` | - | - | `Password123!` |
| `customer.ali` | Ali Bayram | `ali.bayram@example.local` | `CUSTOMER` | - | - | `Password123!` |
| `lead.identity` | Irem Gunes | `irem.gunes@example.local` | `AGENT` | `ACCESS_MANAGEMENT` | `IDENTITY_OPERATIONS` | `Password123!` |
| `lead.permission` | Cem Arslan | `cem.arslan@example.local` | `AGENT` | `ACCESS_MANAGEMENT` | `PERMISSION_OPERATIONS` | `Password123!` |
| `lead.web` | Seda Yildirim | `seda.yildirim@example.local` | `AGENT` | `APPLICATION_SUPPORT` | `WEB_APP_SUPPORT` | `Password123!` |
| `lead.core` | Okan Demir | `okan.demir@example.local` | `AGENT` | `APPLICATION_SUPPORT` | `CORE_APP_SUPPORT` | `Password123!` |
| `lead.network` | Derya Korkmaz | `derya.korkmaz@example.local` | `AGENT` | `INFRASTRUCTURE` | `NETWORK_OPERATIONS` | `Password123!` |
| `lead.platform` | Alp Kaya | `alp.kaya@example.local` | `AGENT` | `INFRASTRUCTURE` | `PLATFORM_OPERATIONS` | `Password123!` |
| `lead.billing` | Melis Acar | `melis.acar@example.local` | `AGENT` | `FINANCE_OPERATIONS` | `BILLING_OPERATIONS` | `Password123!` |
| `lead.payment1` | Bora Yalcin | `bora.yalcin@example.local` | `AGENT` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS_1` | `Password123!` |
| `lead.payment2` | Eren Koc | `eren.koc@example.local` | `AGENT` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS_2` | `Password123!` |
| `agent.identity` | Elif Aydin | `elif.aydin@example.local` | `AGENT` | `ACCESS_MANAGEMENT` | `IDENTITY_OPERATIONS` | `Password123!` |
| `agent.permission` | Mert Kaya | `mert.kaya@example.local` | `AGENT` | `ACCESS_MANAGEMENT` | `PERMISSION_OPERATIONS` | `Password123!` |
| `agent.web` | Deniz Arslan | `deniz.arslan@example.local` | `AGENT` | `APPLICATION_SUPPORT` | `WEB_APP_SUPPORT` | `Password123!` |
| `agent.core` | Selin Demir | `selin.demir@example.local` | `AGENT` | `APPLICATION_SUPPORT` | `CORE_APP_SUPPORT` | `Password123!` |
| `agent.network` | Baran Yilmaz | `baran.yilmaz@example.local` | `AGENT` | `INFRASTRUCTURE` | `NETWORK_OPERATIONS` | `Password123!` |
| `agent.platform` | Ece Sahin | `ece.sahin@example.local` | `AGENT` | `INFRASTRUCTURE` | `PLATFORM_OPERATIONS` | `Password123!` |
| `agent.billing` | Onur Demir | `onur.demir@example.local` | `AGENT` | `FINANCE_OPERATIONS` | `BILLING_OPERATIONS` | `Password123!` |
| `agent.payment` | Zeynep Ozturk | `zeynep.ozturk@example.local` | `AGENT` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS_1` | `Password123!` |
| `agent.payment2` | Seda Erdem | `seda.erdem@example.local` | `AGENT` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS_2` | `Password123!` |
| `manager.user` | Deniz Karaca | `deniz.karaca@example.local` | `MANAGER` | - | - | `Password123!` |
| `admin.user` | Burak Ozkan | `burak.ozkan@example.local` | `ADMIN` | - | - | `Password123!` |

These credentials are local development placeholders only. They must not be used
outside local/demo environments.

Agent user ids in this realm export intentionally match the `actor_id` values
seeded by ticket-service `team_members`. This keeps local authorization aligned
with the DB-authoritative team membership decision.

`ticket-dev-cli` has direct access grants enabled only for local automated
verification. Web and mobile clients must use Authorization Code + PKCE.

## Import

Docker Compose mounts `ticket-management-realm.json` and starts Keycloak with
`--import-realm`.

Docker Compose also mounts `themes/ticket` as a custom login theme. The theme
keeps web and mobile clients on Authorization Code + PKCE; user passwords are
submitted only to Keycloak, not to the React, React Native, or backend services.

Keycloak imports this realm into an empty local volume. If a local
`ticket-v2-keycloak` volume already exists, user changes in the export are not
automatically replayed; recreate or manually sync the local realm when demo
users change.

The local demo reset script syncs these users through Keycloak Admin REST when
Keycloak is running:

```powershell
.\scripts\reset-local-demo-data.ps1 -ConfirmReset
```

The script updates or creates fixed-ID demo users through Keycloak Admin REST.
If a local realm already contains the same username with a different id,
recreate the local Keycloak container so the expected `sub` value is restored.
