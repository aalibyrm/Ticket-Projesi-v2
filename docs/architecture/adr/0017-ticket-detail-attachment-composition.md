# ADR-0017: Ticket Detail Attachment Composition

## Karar

Ticket detail response'u attachment metadata listesini de dondurur. Bu listeyi
`ticket-service`, kendi authorization kontrolunu yaptiktan sonra `file-service`
internal attachment metadata endpoint'inden okur.

Bu karar bir API composition yaklasimidir; ticket-service dosya domain'ini veya
object storage kurallarini sahiplenmez.

## Neden

Ticket detayini kullanan client tek response icinde ticket bilgisi ve dosya
metadata'sini gorebilmelidir. Ancak dosya metadata'sinin sahibi file-service
olarak kalmalidir. Ticket-service'in file-service veritabanina dogrudan
baglanmasi veya dosya metadata tablosunu kendi tarafinda tutmasi servis
sinirlarini bozar.

## Alternatifler

- Client'in ticket detail ve attachment listesini iki ayri endpoint ile almasi:
  Servis bagimliligi daha azdi, ancak ticket detail kabul kriterini frontend'e
  orchestration yukleyerek karsilardi.
- Ticket-service icinde attachment projection tutmak: Event-driven mimari icin
  uzun vadede guclu, ancak Kafka/outbox fazi tamamlanmadan bu issue icin fazla
  karmasikti.
- Ticket-service'in file metadata tablosuna direkt erismesi: Hizi yuksek olsa
  da mikroservis veri sahipligi ilkesini bozar ve monolitik coupling yaratirdi.

## Sonuc

`ticket-service` sadece metadata okur; presigned URL, object key, validation ve
storage kurallari `file-service` icinde kalir. File-service internal endpoint
presigned URL veya object key dondurmez. Gateway yalnizca `/api/files/**`
route'unu expose eder; internal endpointler ileride service-to-service auth ile
sikilastirilacaktir.
