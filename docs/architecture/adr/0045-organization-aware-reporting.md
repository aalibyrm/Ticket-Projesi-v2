# ADR-0045: Organization Aware Reporting

## Karar

Kullanici #65 icin A secenegiyle ilerlemeyi secti: organization routing etkisi
reporting-service read modeline simdi eklenecek. Department ve team kirilimlari
ticket-service DB'sinden senkron okunmayacak; Kafka eventlerinden
`reporting_schema.ticket_report_projection` tablosuna kopyalanacak.

`ticket.created` v1 payload'i backward-compatible sekilde `topicCode`,
`topicName`, `routedDepartmentId`, `routedDepartmentCode` ve
`routedDepartmentName` alanlariyla genisletildi. Eski eventlerde bu alanlar null
olabilir. `ticket.assigned` eventindeki team-only assignment reporting tarafinda
desteklenir.

## Degerlendirilen Secenekler

- A: Reporting read model'e organization dimension eklemek. Secildi; manager
  raporlari department/team kirilimini event-driven ve servis sinirlarina uygun
  sekilde gosterir.
- B: Sadece dokumante edip final smoke #56 oncesine ertelemek. Daha hizliydi,
  fakat sprint 10 routing etkisi reporting tarafinda kanitlanmamis kalirdi.
- C: Reporting-service'in ticket-service organization API'larini sorgulayarak
  raporu runtime enrich etmesi. Daha zengin label uretirdi, fakat rapor
  endpointlerini ticket-service latency ve availability durumuna baglardi.

## Guvenlik Etkisi

- Manager raporu aggregate department/team sayisi dondurur; customer veya ticket
  icerigi tasimaz.
- Ticket `summary`, `description`, yorum, internal note ve dosya bilgisi event
  payload'ina eklenmez.
- Rapor endpoint yetkisi `reporting-service` tarafinda `MANAGER` veya `ADMIN`
  rolu ile uygulanir.
- Ticket-level erisim yetkisi hala `ticket-service` sorumlulugundadir.

## Sonuc

#65 ile `ticket_report_projection` topic ve routed department alanlarini
saklar. Status distribution API'si status sayilarina ek olarak
`departmentCounts` ve `teamCounts` alanlarini dondurur. Web manager ekraninda bu
alanlar department ve team dagilimi olarak gorunur.
