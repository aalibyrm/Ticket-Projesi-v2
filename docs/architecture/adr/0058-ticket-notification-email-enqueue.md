# ADR-0058: Ticket notification email enqueue

## Status
Accepted

## Context
Notification-service zaten UI notification kaydi uretiyor ve Mailpit destekli e-posta
delivery altyapisina sahipti. Ancak ticket event consumer akisi UI notification ile
sinirli kaldigi icin `ticket.created` ve `ticket.external-comment-added` eventleri
Mailpit'e gidecek email delivery kaydi olusturmuyordu.

## Decision
`ticket.created` ve `ticket.external-comment-added` eventleri islenirken UI
notification yaninda idempotent email delivery de kuyruga alinacak.

Recipient bilgisi notification-service icinde demo profile directory ile cozulur.
Bilinen demo kullanicilar okunabilir isim ve e-posta ile, bilinmeyen local Keycloak
actor ID'leri ise deterministik `user-<short-id>@example.local` fallback adresiyle
islenir. Bu fallback sadece local/demo smoke akisini kesmemek icindir.

External comment event payload'i mesaj govdesini tasimadigi icin e-posta template'ine
yorum metni eklenmez; sabit, guvenli bir "new message available" ozeti kullanilir.

## Consequences
- Mailpit lokal ortamda ticket olusturma ve dis mesaj bildirimlerini gosterebilir.
- Kafka duplicate delivery durumunda ayni event/template/recipient icin tek email
  delivery kalir.
- Notification-service henuz gercek identity owner degildir; production'da bu demo
  directory yerine identity/profile servisi veya Keycloak admin/profile projection'i
  eklenmelidir.
- Event payload'inda mesaj govdesi tasinmadigi icin PII sizintisi ve template XSS
  riski dusuk tutulur.
