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

## API Standartlari

- Tum endpointler gateway arkasindan `/api/...` prefix'i ile yayinlanir.
- Request validation zorunludur.
- Error response formati standarttir.
- Internal exception detayi client'a sizdirilmaz.
- Authorization karari servis tarafinda da uygulanir; gateway tek guvenlik
  siniri kabul edilmez.

## Event Standartlari

Kafka eventleri minimum veri tasir:

- `eventId`
- `eventType`
- `version`
- `occurredAt`
- `actorId`
- `aggregateId`
- `payload`

Hassas veri, dosya icerigi, access token veya presigned URL event icine
koyulmaz.

