# ADR-0028: Config Tabanli SLA Policy

## Karar

SLA deadline hesaplamasi icin A secenegi secildi: LOW, MEDIUM ve HIGH policy
sureleri `workflow-sla-service` config degerlerinden okunacak; her ticket icin
hesaplanan SLA state ise `workflow_schema.sla_ticket_states` tablosunda
saklanacak.

Varsayilan hedef cozum sureleri:

- LOW: 72 saat
- MEDIUM: 24 saat
- HIGH: 8 saat

## Degerlendirilen Secenekler

- Config tabanli policy: Sade, hizli ve operasyonel olarak dusuk maliyetli.
  Policy degisikligi config/deploy gerektirir.
- DB tabanli policy: Runtime'da degistirilebilir ve daha kurumsaldir, fakat
  policy yonetimi, admin API/UI ve audit gerektirir.
- Mesai/takvim tabanli policy: En gercekci SLA modelidir, fakat tatil/mesai
  takvimi, timezone ve exception gunleri nedeniyle bu faz icin fazla
  karmasiktir.

## Neden

#36 kabul kriteri, priority bazli deadline hesaplanmasini ve state'in ticket
bazinda saklanmasini istiyor. Config tabanli policy bu ihtiyaci mikroservis
sinirini bozmadan karsilar ve #37 risk/breach detection icin gerekli kalici
state tablosunu hazirlar.

## Sonuc

`ticket.created` eventi workflow-sla-service tarafindan idempotent sekilde
islenir. Event `occurredAt` degeri ticket acilis zamani kabul edilir; target
deadline `occurredAt + priority policy duration` olarak hesaplanir. Duplicate
event delivery `processed_events` tablosuyla engellenir.
