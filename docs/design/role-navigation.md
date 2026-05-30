# Role Navigation

Bu dokuman role-aware shell davranisini tanimlar. Frontend navigation
gorunurlugu kullanici deneyimi icindir; yetki kararinin kaynagi backend
authorization'dir.

## Shell

Tum roller ayni temel shell'i kullanir:

- Fixed `72px` sidebar
- Top header
- Main workspace
- Notification, help ve profile kontrolleri

Sidebar icon-only olur. Her icon button icin tooltip ve `aria-label` gerekir.

## Navigation Matrix

| Route / Nav Item | Customer | Agent | Manager | Admin |
| --- | --- | --- | --- | --- |
| `/tickets` Taleplerim | Yes | No | No | No |
| `/tickets/new` Yeni talep | Yes | No | No | No |
| `/tickets/:ticketId` Ticket detail | Own only | Assigned/team only | Read if allowed | Admin if allowed |
| `/agent/inbox` Atanan biletler | No | Yes | Optional read/monitor | Optional |
| `/agent/messages/:ticketId` Mesajlar | No | Yes | Optional read/monitor | Optional |
| `/reports` Yonetici raporu | No | No | Yes | Yes |
| `/admin/settings` Ayarlar | No | No | No | Yes |
| `/notifications` Bildirimler | Yes | Yes | Yes | Yes |
| `/profile` Profil | Yes | Yes | Yes | Yes |

## Sidebar Items

| Icon intent | Customer | Agent | Manager | Admin |
| --- | --- | --- | --- | --- |
| Dashboard/home | `/tickets` | `/agent/inbox` | `/reports` | `/admin/settings` |
| Inbox/tickets | `/tickets` | `/agent/inbox` | `/agent/inbox` optional | `/agent/inbox` optional |
| Reports/analytics | Hidden | Hidden | `/reports` | `/reports` |
| Users/customers | Hidden | Hidden | Optional | `/admin/users` |
| Settings | Hidden | Hidden | Hidden | `/admin/settings` |

## Header Behavior

- Search:
  - Customer: ticket subject/id search.
  - Agent: ticket queue, message and customer search.
  - Manager: report-level filtering and service/agent search.
  - Admin: user/config search.
- Notifications: all roles.
- Help: all roles.
- Profile avatar/menu: all roles.

## Protected Route Rules

- Unauthenticated users are redirected to login.
- Authenticated users without route role access see a forbidden state, not a
  blank page.
- Forbidden state must not reveal ticket/customer details.
- After login, default redirect is role-aware:
  - Customer -> `/tickets`
  - Agent -> `/agent/inbox`
  - Manager -> `/reports`
  - Admin -> `/admin/settings`

## Backend Alignment

Frontend role checks are duplicated for UX only. Backend remains authoritative:

- Customer ownership is enforced by `ticket-service`.
- Attachment access is authorized by `ticket-service` before `file-service`
  creates upload/download URLs.
- Manager report access is enforced by `reporting-service`.
- Admin-only settings remain backend-protected.
