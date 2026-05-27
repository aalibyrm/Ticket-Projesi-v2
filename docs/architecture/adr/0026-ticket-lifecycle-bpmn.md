# ADR-0026: Ticket Lifecycle BPMN Model

## Karar

Ticket lifecycle, `workflow-sla-service` icinde `ticketLifecycle` adli BPMN
process'i ile modellenecek. Process, signal tabanli status gecislerini
destekleyecek:

- `START_PROGRESS`
- `REQUEST_CUSTOMER_INFO`
- `CUSTOMER_RESPONDED`
- `RESOLVE_TICKET`
- `CLOSE_TICKET`
- `REOPEN_TICKET`

## Degerlendirilen Secenekler

- Tam lineer akis: `NEW -> IN_PROGRESS -> WAITING_FOR_CUSTOMER -> RESOLVED ->
  CLOSED` en basit diyagram olur, fakat musteri yaniti sonrasi calismaya donme
  veya cozum reddi gibi gercek ticket davranislarini temsil etmez.
- Signal tabanli state machine: BPMN process instance statuslarda bekler ve
  sadece izinli signal geldikce ilerler. Diyagram biraz daha buyur ama #35'te
  ticket-service adapter entegrasyonuna daha uygun olur.
- Kod icinde enum transition tablosu: Uygulamasi hizli olur, fakat jBPM/BPMN
  isterini ve ADR-0006 kararini dokuman seviyesinde birakir.

## Neden

Kurumsal ticket sisteminde `WAITING_FOR_CUSTOMER` statusu gecici bekleme
noktasi, `RESOLVED` ise kabul veya reopen karari bekleyen noktadir. Bu nedenle
lineer akisa iki kontrollu geri donus eklendi:

- `WAITING_FOR_CUSTOMER -> IN_PROGRESS`
- `RESOLVED -> IN_PROGRESS`

`CLOSED` terminal tutuldu. Bu tercih raporlama, SLA kapanis olcumu ve audit
yorumunu basitlestirir.

## Sonuc

BPMN dosyasi `services/workflow-sla-service/src/main/resources/processes` altina
eklendi ve Kogito runtime testinde `ticketLifecycle` process ID'si dogrulandi.
Izinli gecisler ayrica `docs/architecture/workflow/ticket-lifecycle.md`
dosyasinda tablo olarak tutulur.
