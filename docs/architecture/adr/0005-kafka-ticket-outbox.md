# ADR-0005: Kafka ve Ticket Outbox Pattern

## Karar

Servisler arasi olay akisi Kafka ile kurulacak. Kritik ticket eventleri icin
`ticket-service` sade Outbox Pattern kullanacak.

## Neden

Ticket DB kaydi basarili olup Kafka publish basarisiz olursa notification, SLA ve
reporting servisleri olaydan habersiz kalabilir. Outbox, ticket yazimi ve event
kaydini ayni transaction icinde tutarak bu riski azaltir.

## Sonuc

Direkt Kafka publish'e gore 2-4 gun ek gelistirme maliyeti vardir. Buna karsilik
TicketCreated, TicketStatusChanged, TicketAssigned, ExternalCommentAdded ve
WorklogAdded gibi kritik eventler daha guvenilir yayilir.

