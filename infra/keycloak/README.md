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

| Username | Role | Password |
| --- | --- | --- |
| `customer.user` | `CUSTOMER` | `Password123!` |
| `agent.user` | `AGENT` | `Password123!` |
| `manager.user` | `MANAGER` | `Password123!` |
| `admin.user` | `ADMIN` | `Password123!` |

These credentials are local development placeholders only. They must not be used
outside local/demo environments.

`ticket-dev-cli` has direct access grants enabled only for local automated
verification. Web and mobile clients must use Authorization Code + PKCE.

## Import

Docker Compose mounts `ticket-management-realm.json` and starts Keycloak with
`--import-realm`.

Docker Compose also mounts `themes/ticket` as a custom login theme. The theme
keeps web and mobile clients on Authorization Code + PKCE; user passwords are
submitted only to Keycloak, not to the React, React Native, or backend services.
