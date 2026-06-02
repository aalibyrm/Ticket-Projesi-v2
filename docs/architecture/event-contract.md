# Kafka Event Contract

Bu dokuman Kafka event sozlesmesinin servisler arasinda nasil paylasilacagini
tanimlar. Uygulama icinde canonical contract `libs/event-contract` Java module'u
olacaktir.

## Secilen Yaklasim

Projede #21 icin B secenegi secildi: Ortak Java event contract module'u.

Bu karar su nedenlerle verildi:

- Proje Java-only kalacak; Java disi generator, schema tool veya runtime
  bagimliligi eklenmeyecek.
- Producer ve consumer servisler envelope, topic ve event type hatalarini
  compile-time seviyesinde yakalayabilecek.
- AsyncAPI/JSON Schema yaklasimi kurumsal olsa da bu asamada dis consumer
  olmadigi icin surec ve drift maliyeti olusturacaktir.
- Overengineering'i sinirlamak icin schema registry, code generation ve
  compatibility pipeline bu fazda kurulmayacaktir.

AsyncAPI/JSON Schema ileride farkli ekipler, dis sistemler veya Java disi
consumer ihtiyaci dogarsa yeniden degerlendirilecektir.

## Topicler

| Alan | Kafka topic | Sahip servis | Kullanim |
| --- | --- | --- | --- |
| Ticket eventleri | `ticket.events.v1` | `ticket-service` | Ticket lifecycle eventleri |
| Dosya eventleri | `file.events.v1` | `file-service` | Attachment eventleri |
| Workflow/SLA eventleri | `workflow.events.v1` | `workflow-sla-service` | SLA ve workflow eventleri |
| Notification eventleri | `notification.events.v1` | `notification-service` | E-posta teslim/failed eventleri |

Topic version'i topic adinin sonunda tutulur. Breaking change yeni topic
version'i gerektirir: `ticket.events.v2`.

## Envelope

Her event `EventEnvelope<T extends EventPayload>` ile yayinlanir.

| Alan | Zorunlu | Aciklama |
| --- | --- | --- |
| `eventId` | Evet | Idempotency ve trace icin UUID |
| `eventType` | Evet | `ticket.created` gibi sabit event adi |
| `version` | Evet | Event schema version'i |
| `occurredAt` | Evet | UTC event olusma zamani |
| `actorId` | Evet | Islemi baslatan kullanici veya sistem aktoru |
| `aggregateType` | Evet | `ticket`, `attachment`, `sla`, `notification` |
| `aggregateId` | Evet | Eventin ait oldugu aggregate UUID |
| `correlationId` | Hayir | Request/trace correlation degeri |
| `payload` | Evet | Minimal event verisi |

Ornek:

```json
{
  "eventId": "5a1567f2-8bd1-4dd9-8734-fda624bf740f",
  "eventType": "ticket.created",
  "version": 1,
  "occurredAt": "2026-05-27T10:15:30Z",
  "actorId": "71f5f5e3-f9bd-4314-a210-4a13d668f98e",
  "aggregateType": "ticket",
  "aggregateId": "33dd4498-e998-48b4-bb2f-cf340bb20444",
  "correlationId": "request-123",
  "payload": {
    "ticketId": "33dd4498-e998-48b4-bb2f-cf340bb20444",
    "priority": "HIGH"
  }
}
```

## Version Politikasi

- Mevcut desteklenen version: `1`.
- Backward-compatible alan eklemeleri ayni version icinde yapilabilir.
- Alan silme, anlam degistirme veya tip degistirme breaking change sayilir.
- Breaking change icin yeni event version'i ve gerekiyorsa yeni topic version'i
  acilir.
- Consumer servisler bilmedigi version'i sessizce islememeli; loglayip
  idempotency kaydiyla reddetmelidir.

## Ticket Event Payloadlari

### `ticket.created`

`TicketCreatedPayload` sadece routing ve consumer kararlarini destekleyen
minimum alani tasir:

- `ticketId`
- `ticketNumber`
- `customerId`
- `productId`
- `topicCode` (opsiyonel; #65 sonrasi yeni eventlerde doludur)
- `topicName` (opsiyonel)
- `routedDepartmentId` (opsiyonel; #65 sonrasi yeni eventlerde doludur)
- `routedDepartmentCode` (opsiyonel)
- `routedDepartmentName` (opsiyonel)
- `priority`
- `status`

Organization alanlari backward-compatible olarak eklenmistir. Eski v1 eventleri
bu alanlari tasimayabilir; consumer'lar null degeri desteklemelidir.

Ticket `summary` ve `description` event payload'ina koyulmaz. Bu alanlar
kullanici tarafindan yazildigi icin PII veya hassas veri icerebilir.

### `ticket.status-changed`

`TicketStatusChangedPayload` alanlari:

- `ticketId`
- `ticketNumber`
- `previousStatus`
- `newStatus`

### `ticket.assigned`

`TicketAssignedPayload` alanlari:

- `ticketId`
- `ticketNumber`
- `assigneeId` (team-only assignment icin null olabilir)
- `assignedTeamId`

`assigneeId` ve `assignedTeamId` ayni anda null olamaz. Ticket create sirasinda
otomatik topic routing yalniz `assignedTeamId` uretebilir; agent atamasi daha
sonra ayri assignment aksiyonu ile yapilir.

### `ticket.external-comment-added`

`ExternalCommentAddedPayload` alanlari:

- `ticketId`
- `ticketNumber`
- `commentId`
- `authorId`

Yorum metni event payload'ina koyulmaz. Consumer gerekirse ticket-service API'si
uzerinden yetkili okuma yapmalidir.

### `ticket.worklog-added`

`WorklogAddedPayload` alanlari:

- `ticketId`
- `ticketNumber`
- `worklogId`
- `agentId`
- `workDate`
- `durationMinutes`

Worklog aciklamasi event payload'ina koyulmaz.

## Workflow/SLA Event Payloadlari

### `workflow.sla-risk-detected`

`SlaRiskDetectedPayload` alanlari:

- `ticketId`
- `ticketNumber`
- `recipientId`
- `priority`
- `targetResolutionAt`
- `detectedAt`
- `riskReason`

Risk eventi ticket icerigini tasimaz; notification-service sadece hedef
kullanici ve template modeli icin gerekli minimum alanlari alir.

### `workflow.sla-breach-detected`

`SlaBreachedPayload` alanlari:

- `ticketId`
- `ticketNumber`
- `recipientId`
- `priority`
- `targetResolutionAt`
- `detectedAt`
- `breachReason`

Breach eventi ticket aciklamasi, yorum, internal note veya dosya bilgisini
tasimaz.

## Minimal Payload Politikasi

Event payload sadece consumer'in karar vermesi icin gereken minimum veriyi
tasir. Domain detaylari gerekiyorsa consumer ilgili servis API'sinden okur.

Event payload icinde su veriler tasinmaz:

- password, token, access token, refresh token
- presigned URL, object storage key
- dosya icerigi veya buyuk text blob
- secret/private key gibi gizli degerler

Bu kurallar `EventPayloadPolicy` icinde ortak field-name guard olarak da
paylasilir. Bu guard tek basina serialization security yerine gecmez; producer
servisler DTO tasariminda da minimum veri ilkesini uygulamalidir.
