# ADR-0027: Ticket Workflow Adapter

## Karar

`ticket-service`, status degisimlerini dogrudan enum mutasyonu ile degil
`TicketWorkflowPort` uzerinden calistiracak. Ilk adapter,
`ticketLifecycle` BPMN dosyasindaki izinli gecis tablosunu lokal olarak enforce
eder ve her gecise karsilik gelen BPMN signal adini dondurur.

## Degerlendirilen Secenekler

- Lokal BPMN uyumlu adapter: `ticket-service` icinde port + adapter ile izinli
  gecisler hemen enforce edilir. HTTP, process instance persistence ve dagitik
  transaction karmasasi bu issue'ya girmez.
- Senkron workflow-service HTTP cagrisi: BPMN runtime tek karar kaynagi olur,
  fakat workflow API, process instance id esleme, timeout/retry ve ticket DB
  transaction'i ile dagitik tutarlilik kararlarini ayni anda gerektirir.
- Kafka command/reply: Servisler gevsek baglanir, fakat status degisikligi icin
  kullaniciya anlik valid/invalid cevap verme ihtiyaci nedeniyle fazla
  karmasiklasir.

## Neden

#35 kabul kriteri invalid gecislerin reddedilmesini ve valid gecislerde event
uretilmesini istiyor. Lokal adapter, #34 BPMN kararini ticket-service icinde
hemen enforce eder ve mevcut transactional outbox davranisini korur. Remote
Kogito orchestration daha sonra process instance lifecycle ve servisler arasi
timeout politikalari netlesince eklenebilir.

## Sonuc

`NEW -> CLOSED`, `CLOSED -> IN_PROGRESS` gibi BPMN disi gecisler
`INVALID_TICKET_OPERATION` hatasi alir ve outbox'a status event yazilmaz. Valid
gecisler once adapter'da onaylanir, sonra ticket status'u guncellenir ve mevcut
`ticket.status-changed` outbox eventi ayni transaction icinde uretilir.
