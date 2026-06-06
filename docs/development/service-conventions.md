# Service Conventions

## Servis Isimleri

Servis isimleri kebab-case kullanir:

- `api-gateway`
- `ticket-service`
- `workflow-sla-service`
- `file-service`
- `notification-service`
- `reporting-service`

## Paket Isimleri

Java paketleri `com.ticketmanagement.<service>` seklinde baslar.

Ornek:

```text
com.ticketmanagement.ticket
com.ticketmanagement.notification
```

## Katmanlar

Backend servislerinde temel katmanlar:

- `api`: controller, request/response DTO
- `application`: use case service ve port'lar
- `domain`: entity, value object, enum, domain rule
- `infrastructure`: persistence, messaging, external adapter
- `config`: Spring configuration

## Lombok Kullanimi

Lombok tum backend servislerinde standarttir.

- Spring bean constructor injection: `@RequiredArgsConstructor`
- Basit config/property holder siniflari: `@Getter`, `@Setter`
- JPA entity: kontrollu `@Getter` ve `@Setter`
- DTO: mumkunse Java `record`

JPA entity'lerinde `@Data`, kontrolsuz `@EqualsAndHashCode` ve kontrolsuz
`@ToString` kullanilmaz. Bu kural lazy loading yan etkilerini, hassas veri
loglamayi ve domain kimligi hatalarini engellemek icindir.

## API Standartlari

- Public endpointler gateway arkasindan `/api/v1/...` prefix'i ile yayinlanir.
  Servis-local controller path'leri bu fazda `/api/...` kalir ve gateway v1
  prefix'ini servis-local path'e rewrite eder.
- Servisler kendi OpenAPI JSON ciktisini `/v3/api-docs` uzerinden uretir.
  Spec sadece public `/api/**` controllerlarini kapsar ve dokumanda path'ler
  gateway kontratiyla uyumlu olarak `/api/v1/**` gorunur.
- Gateway Swagger UI `/swagger-ui.html` uzerinden servis spec'lerini aggregate
  eder. Production ortaminda gerekirse dokuman endpointleri Springdoc env
  flag'leri ile kapatilabilir.
- Request validation zorunludur.
- Error response formati standarttir.
- Internal exception detayi client'a sizdirilmaz.
- Authorization karari servis tarafinda da uygulanir; gateway tek guvenlik
  siniri kabul edilmez.

## Service ve Controller Yorumlari

Controller ve service siniflarinda endpoint/use-case fonksiyonlarinin ustunde
kisa yorum satiri bulunur. Yorum, fonksiyonun ne is yaptigini tek cumlede
aciklar.

Ornek:

```java
// Musterinin kendi ticket listesini getirir.
List<TicketResponse> listOwnTickets(UUID customerId) {
    ...
}
```

## Event Standartlari

Kafka event sozlesmesinin canonical kaynagi `libs/event-contract` module'udur.
Producer ve consumer servisler topic, event type, envelope ve version kurallarini
bu module uzerinden paylasir.

Kafka eventleri minimum veri tasir:

- `eventId`
- `eventType`
- `version`
- `occurredAt`
- `actorId`
- `aggregateType`
- `aggregateId`
- `correlationId`
- `payload`

Hassas veri, dosya icerigi, access token veya presigned URL event icine
koyulmaz.
