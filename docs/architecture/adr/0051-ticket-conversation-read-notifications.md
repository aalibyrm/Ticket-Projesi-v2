# ADR-0051: Ticket Conversation Read State And Notifications

## Status

Accepted for #70.

## Context

Customer ve agent'lar ticket uzerinden iletisim kurmali. Mevcut sistemde
`ticket_comments` modeli, customer-visible external yorumlar ve support-only
internal notlar zaten ticket-service tarafinda tutuluyor. Ticket erisim
yetkisi de ticket-service tarafindan verildigi icin ayri bir conversation-service
bu asamada veri sahipligi ve authorization maliyetini artirir.

## Decision

Kullanici B secenegini secti: mevcut comment modeli korunacak, ticket-service
conversation read/unread state'inin authoritative sahibi olacak ve external
comment eventleri notification-service tarafindan UI notification'a
cevrilecek.

Canli guncelleme transport'u bu kararin parcasi olarak zorlanmaz. Frontend
uygulamasi icin polling, SSE veya WebSocket ayrimi sonraki uygulama adiminda
kullanici tarafindan secilecektir.

## Consequences

- Customer sadece kendi ticket'indaki external yorumlari okur ve okundu
  isaretler.
- Support actor sadece ticket-service'in izin verdigi ticket mesajlarini okur
  ve okundu isaretler.
- Internal notes customer'a gosterilmez ve customer notification'i uretmez.
- External comment event payload'i karsi taraf notification hedefini uretmeye
  yetecek ticket context'i ile genisletilir.
- Ayri conversation-service acilmadigi icin mikroservis sinirlari sade kalir;
  ileride yuksek hacimli real-time ihtiyac dogarsa yeni service karari tekrar
  acilabilir.

## Security

Read/unread endpointleri mevcut ticket sahipligi ve support DB membership
kurallarini yeniden kullanir. Client tarafindan gelen actor, team veya role
header'lari JWT aktif ortamda authoritative kabul edilmez. Notification-service
internal notes veya yorum body icerigini UI notification mesajina tasimaz.
