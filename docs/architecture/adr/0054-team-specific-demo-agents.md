# ADR-0054: Team Specific Demo Agents

## Status

Accepted

## Context

Ticket routing `topic -> department -> team` kuraliyla yapiliyor ve agent
yetkisi ticket-service tarafinda DB kaynakli `team_members` kayitlarina gore
belirleniyor. Tek bir generic `agent.user` hesabi Keycloak `sub` degeri ile DB
`team_members.actor_id` seed degerleri eslesmedigi icin local demo akisini
yaniltiyordu.

## Decision

Kullanici C secenegini secti: her support team icin ayri demo agent hesabi
olacak.

Keycloak realm export'unda agent user id'leri ticket-service seed actor
degerleriyle birebir eslestirilir:

| User | Department | Team | Actor ID |
| --- | --- | --- | --- |
| `agent.identity` | `ACCESS_MANAGEMENT` | `IDENTITY_OPERATIONS` | `40000000-0000-0000-0000-000000000001` |
| `agent.permission` | `ACCESS_MANAGEMENT` | `PERMISSION_OPERATIONS` | `40000000-0000-0000-0000-000000000002` |
| `agent.web` | `APPLICATION_SUPPORT` | `WEB_APP_SUPPORT` | `40000000-0000-0000-0000-000000000003` |
| `agent.core` | `APPLICATION_SUPPORT` | `CORE_APP_SUPPORT` | `40000000-0000-0000-0000-000000000004` |
| `agent.network` | `INFRASTRUCTURE` | `NETWORK_OPERATIONS` | `40000000-0000-0000-0000-000000000005` |
| `agent.platform` | `INFRASTRUCTURE` | `PLATFORM_OPERATIONS` | `40000000-0000-0000-0000-000000000006` |
| `agent.billing` | `FINANCE_OPERATIONS` | `BILLING_OPERATIONS` | `40000000-0000-0000-0000-000000000007` |
| `agent.payment` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS_1` | `40000000-0000-0000-0000-000000000008` |
| `agent.payment2` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS_2` | `40000000-0000-0000-0000-000000000009` |

## Consequences

- Local smoke testing is explicit: a `PAYMENT_FAILURE` ticket is tested with
  `agent.payment`, not a generic agent.
- DB authoritative team authorization stays intact; we do not add broad
  `team_ids` claims or weaken gateway/backend access rules.
- Existing local Keycloak volumes may still contain older demo users until the
  realm is reset or users are synced manually.
- Team lead specific management scenarios are covered later by ADR-0066.
