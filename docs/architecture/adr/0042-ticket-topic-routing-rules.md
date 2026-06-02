# ADR-0042: Ticket Topic Routing Rules

## Karar

Kullanici #62 kapsaminda ticket topic katalogu icin B secenegini secti:
kurumsal ama kontrollu sekiz topic kullanilacak. Customer ticket acarken
`topicCode` gonderir; `ticket-service` aktif DB routing rule uzerinden
`topic -> department -> default team` cozumler.

Seed edilen topic ve routing kararlari:

| Topic | Department | Default team |
| --- | --- | --- |
| `PASSWORD_RESET` | `ACCESS_MANAGEMENT` | `IDENTITY_OPERATIONS` |
| `PERMISSION_REQUEST` | `ACCESS_MANAGEMENT` | `PERMISSION_OPERATIONS` |
| `WEB_PORTAL_BUG` | `APPLICATION_SUPPORT` | `WEB_APP_SUPPORT` |
| `CORE_SYSTEM_ERROR` | `APPLICATION_SUPPORT` | `CORE_APP_SUPPORT` |
| `NETWORK_CONNECTIVITY` | `INFRASTRUCTURE` | `NETWORK_OPERATIONS` |
| `SERVER_PLATFORM` | `INFRASTRUCTURE` | `PLATFORM_OPERATIONS` |
| `INVOICE_ISSUE` | `FINANCE_OPERATIONS` | `BILLING_OPERATIONS` |
| `PAYMENT_FAILURE` | `FINANCE_OPERATIONS` | `PAYMENT_OPERATIONS` |

Kullanici API contract icin B secenegini secti: request `topicCode` tasir.
UUID tabanli `topicId` yerine business code secilmesinin nedeni frontend ve
debug akisini okunur tutmaktir. Topic code degisikligi API sozlesmesini
etkileyecegi icin code alanlari stabil domain sozlesmesi kabul edilir.

Kullanici event contract icin B secenegini secti: `ticket.assigned` eventinde
`assigneeId` nullable olabilir. Otomatik routing create aninda sadece team
atamasi uretebilir; bu nedenle team-only assignment gecerli lifecycle olayi
olarak kabul edilir.

Not: #65 ve ADR-0045 ile reporting ihtiyaci icin `ticket.created` payload'i
opsiyonel topic ve routed department alanlariyla genisletildi. Bu, bu ADR'deki
"routing alanlarini create eventine koymama" tercihinin sinirli ve
backward-compatible reporting dimension ihtiyaci icin kismen guncellenmis
halidir. Summary/description gibi hassas veya buyuk alanlar hala event
payload'ina eklenmez.

## Degerlendirilen Secenekler

### Topic katalogu

- A: Basit department bazli topic seti. Hizliydi ama raporlama ve routing
  ayrimi zayif kalirdi.
- B: Kurumsal ama kontrollu topic seti. Secildi; sekiz topic ile her uzman ekip
  icin net default route verir.
- C: Urun + topic bilesimli set. Daha detayliydi ancak topic sayisini hizla
  buyutur ve bu faz icin gereksiz form karmasasi olustururdu.

### Create request contract

- A: `topicId`. DB odakli ve netti, ancak frontend/debug icin daha okunaksizdi.
- B: `topicCode`. Secildi; stabil business code UI ve testlerde daha anlasilir.
- C: `topicId` veya `topicCode`. Esnekti, ancak validation ve bakim maliyetini
  gereksiz artirirdi.

### Assignment event contract

- A: Create sirasinda assignment event uretmemek. Daha az degisiklik getirirdi
  ancak SLA/reporting projection otomatik takim bilgisini hemen ogrenemezdi.
- B: `ticket.assigned` icinde nullable `assigneeId`. Secildi; team-only
  assignment kurumsal destek sureclerinde normaldir.
- C: Routing alanlarini `ticket.created` payload'una eklemek. Create eventini
  buyutur ve assignment bilgisinin ayri lifecycle event olma ayrimini bulanik
  hale getirirdi.

## Guvenlik Etkisi

- Customer `assignedTeamId`, `assigneeId` veya department override gonderemez.
- Create request icindeki bilinmeyen assignment alanlari domain tarafinda
  kullanilmaz.
- Invalid veya inactive topic/routing rule ticket olusturmaz.
- Routing rule sadece aktif topic, aktif department ve aktif team icin gecerlidir.
- Topic katalogu customer formu icin okunabilir tutulur; routing detaylari
  customer request tarafindan kontrol edilmez.

## Sonuc

#62 ile `ticket_topics`, `ticket_routing_rules`, `tickets.topic_id` ve
`tickets.routed_department_id` ticket-service DB modeline eklendi. Ticket create
akisi `topicCode` ile route cozer, ticket'i default team'e atar ve create
transaction'i icinde `ticket.created` ile `ticket.assigned` eventlerini birlikte
outbox'a kaydeder.
