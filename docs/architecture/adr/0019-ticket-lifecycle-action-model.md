# ADR-0019: Ticket Lifecycle Action Model

## Karar

Ticket lifecycle aksiyonlari icin B secenegi secildi: `ticket-service` icinde
sade operasyon modeli kullanilacak.

Bu modelde assignment bilgisi ticket uzerinde nullable `assignee_id` ve
`assigned_team_id` alanlariyla tutulur. External comment ve worklog ise ticket
aggregate siniri icinde ayri tablolarla saklanir:

- `ticket_comments`
- `ticket_worklogs`

Agent endpointleri bu aksiyonlari uygular ve her aksiyon ayni transaction icinde
versioned outbox event uretir.

## Neden

#24 sadece event isimlerini degil, bu eventleri doguran gercek domain
aksiyonlarini da gerektirir. Sadece event-only model hizli olurdu ama urun
davranisini eksik birakir. Assignment, comment ve worklog'u tamamen ayri
aggregate'lar olarak modellemek ise bu fazda fazla karmasik olurdu.

Secilen sade operasyon modeli, gercek ticket management davranisini saglar,
transactional outbox ile event guvenilirligini korur ve mikroservis sinirini
bozmadan gelistirme karmasikligini sinirlar.

## Alternatifler

- Event-only model: Event sozlesmeleri hizli tamamlanirdi, ancak status,
  assignment, comment ve worklog API'leri gercek davranis uretmezdi.
- Ayri aggregate modeli: Buyuk domainlerde daha esnektir, ancak bu fazda ayri
  repository/service/workflow sinirlari ve ekstra authorization karmasikligi
  getirirdi.

## Sonuc

`ticket-service` su eventleri outbox'a version `1` olarak yazar:

- `ticket.created`
- `ticket.status-changed`
- `ticket.assigned`
- `ticket.external-comment-added`
- `ticket.worklog-added`

Event payload'lari minimum veri tasir. Yorum govdesi, worklog aciklamasi,
ticket summary ve ticket description Kafka event payload'ina koyulmaz.

Assignment modeli geldigi icin file attachment authorization karari da
genisletildi: customer kendi ticket'ina, assigned agent veya actor'un
`team_ids` claim'i icindeki assigned team ise ilgili ticket dosyalarina
erisebilir.
