# API Documentation

Bu dokuman sprint bazli endpoint contract ozetlerini tutar. Ayrintili runtime
OpenAPI dokumani final smoke #56 oncesi uretilecektir.

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
| `GET /api/ticket-topics` | Customer/Agent/Manager/Admin | Aktif ticket topic katalogu |
| `GET /api/organization/departments` | Agent/Manager/Admin | Department ve alt team katalogu |
| `GET /api/organization/teams` | Agent/Manager/Admin | Aktif support team katalogu |
| `GET /api/organization/teams/{teamId}/members` | Agent/Manager/Admin | Secili team'in aktif member listesi |

## Reporting Endpoints

`reporting-service` endpointleri `MANAGER` veya `ADMIN` rolune aciktir.
Frontend route guard sadece UX icindir; backend authorization zorunlu kalir.

### `GET /api/reports/tickets/status-distribution`

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
