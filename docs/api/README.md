# API Documentation

Bu dokuman sprint bazli endpoint contract ozetlerini tutar. Runtime OpenAPI
dokumanlari servis seviyesinde uretilir; bu dosya final smoke ve frontend
contract'lari icin okunabilir API haritasidir.

## API Gateway Public Surface

Public web ve mobil istemciler gateway uzerinden `/api/v1/**` prefix'i ile REST
endpointlerine erisir. Frontend route guard yalnizca UX icindir; asil
authorization gateway ve ilgili servis tarafinda uygulanir.

| Alan | Endpoint | Rol | Sahip servis |
| --- | --- | --- | --- |
| Customer tickets | `GET /api/v1/tickets`, `POST /api/v1/tickets`, `GET /api/v1/tickets/{ticketId}` | Customer/Admin | ticket-service |
| Customer comments | `GET /api/v1/tickets/{ticketId}/comments`, `POST /api/v1/tickets/{ticketId}/comments/external` | Customer/Admin | ticket-service |
| Catalog | `GET /api/v1/products`, `GET /api/v1/ticket-topics` | Customer/Agent/Manager/Admin | ticket-service |
| Files | `POST /api/v1/files/uploads`, `POST /api/v1/files/uploads/{fileId}/complete`, `POST /api/v1/files/{fileId}/download-url` | Customer/Agent/Admin | file-service |
| Notifications | `GET /api/v1/notifications`, `PATCH /api/v1/notifications/{notificationId}/read` | Customer/Agent/Manager/Admin | notification-service |
| Agent tickets | `GET /api/v1/agent/tickets`, `GET /api/v1/agent/tickets/{ticketId}` | Agent/Admin | ticket-service |
| Agent actions | status, assignment, comments, worklogs under `/api/v1/agent/tickets/{ticketId}` | Agent/Admin | ticket-service |
| Organization catalog | `GET /api/v1/organization/departments`, `GET /api/v1/organization/teams`, `GET /api/v1/organization/teams/{teamId}/members` | Agent/Manager/Admin | ticket-service |
| Reports | `/api/v1/reports/**` | Manager/Admin | reporting-service |

## Final Smoke Contract

#56 Playwright smoke testi su contract'lara baglidir:

- Ticket create request customer'dan `productId`, `topicCode`, `summary`,
  `description`, `priority` alir.
- Ticket response attachment metadata dahil tek detail view icin yeterli
  alanlari tasir.
- File upload presigned URL akisi `create upload url -> object storage PUT ->
  complete upload` siralamasini kullanir.
- Agent status update ve external comment aksiyonlari ticket-service event
  uretim yolunu temsil eder.
- Notification response customer-visible baslik/mesaj bilgisini tasir.
- Reporting response'lari status, SLA, closed ticket ve agent performance
  panellerini besler.

Smoke test backend authorization yerine frontend contract regresyonlarini
yakalayacak sekilde browser network doubles kullanir. Backend yetki ve event
guvencesi Maven integration testleriyle korunur.

## Ticket Organization Fields

`ticket-service` ticket response alanlari organization routing bilgisini de
tasir:

| Alan | Tip | Aciklama |
| --- | --- | --- |
| `topicCode` | string/null | Customer'in sectigi ticket topic code degeri |
| `topicName` | string/null | Ticket topic gorunur adi |
| `routedDepartmentId` | UUID/null | Routing rule ile cozulmus department |
| `routedDepartmentCode` | string/null | Department business code |
| `routedDepartmentName` | string/null | Department gorunur adi |
| `assignedTeamId` | UUID/null | Otomatik route veya agent assignment ile atanmis ekip |
| `assigneeId` | UUID/null | Atanmis agent; team-only assignment durumunda null olabilir |

Customer create request sadece `topicCode`, `productId`, `summary`,
`description` ve `priority` gonderir. Customer request'i `assignedTeamId`,
`assigneeId` veya department override tasimaz.

## Organization Catalog Endpoints

| Endpoint | Rol | Aciklama |
| --- | --- | --- |
| `GET /api/v1/ticket-topics` | Customer/Agent/Manager/Admin | Aktif ticket topic katalogu |
| `GET /api/v1/organization/departments` | Agent/Manager/Admin | Department ve alt team katalogu |
| `GET /api/v1/organization/teams` | Agent/Manager/Admin | Aktif support team katalogu |
| `GET /api/v1/organization/teams/{teamId}/members` | Agent/Manager/Admin | Secili team'in aktif member listesi |

## Reporting Endpoints

`reporting-service` endpointleri `MANAGER` veya `ADMIN` rolune aciktir.
Frontend route guard sadece UX icindir; backend authorization zorunlu kalir.

### `GET /api/v1/reports/tickets/status-distribution`

Open ticket status dagilimini ve organization breakdown alanlarini dondurur.

Response alanlari:

| Alan | Tip | Aciklama |
| --- | --- | --- |
| `counts[]` | array | `NEW`, `IN_PROGRESS`, `WAITING_FOR_CUSTOMER`, `RESOLVED` sayilari |
| `departmentCounts[]` | array | Acik ticket sayisi routed department bazinda |
| `teamCounts[]` | array | Acik ticket sayisi assigned team bazinda |
| `totalOpenTickets` | number | Kapali olmayan toplam ticket sayisi |
| `generatedAt` | datetime | Raporun UTC uretim zamani |

`departmentCounts[]` elemani `routedDepartmentId`, `routedDepartmentCode`,
`routedDepartmentName` ve `count` alanlarini tasir. `teamCounts[]` elemani
`assignedTeamId` ve `count` alanlarini tasir.

Reporting verisi event-driven read model oldugu icin operasyonel ticket verisine
gore event gecikmesi kadar eventual consistent olabilir.
