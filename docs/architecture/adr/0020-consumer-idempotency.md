# ADR-0020: Consumer Idempotency Pattern

## Karar

Kafka consumer servisleri duplicate delivery riskine karsi `processed_events`
tablosu kullanir. Her consumer, side effect uretmeden once ayni transaction
icinde `(event_id, consumer_name)` anahtarini `insert ... on conflict do nothing`
ile kaydetmeyi dener.

Insert basariliysa event ilk kez isleniyordur ve side effect calisir. Insert
basarisizsa event duplicate kabul edilir ve side effect calismaz.

## Neden

Kafka publish/consume akisi at-least-once davranir. Network timeout, consumer
restart veya offset commit gecikmesi ayni eventin tekrar teslim edilmesine neden
olabilir. Consumer idempotent degilse ayni notification, email veya projection
guncellemesi birden fazla kez olusur.

`insert ... on conflict do nothing` yaklasimi exception tabanli akistan daha
temizdir ve race condition durumunda database unique key'i karar verici yapar.

## Uygulama

Ilk uygulama `notification-service` icindedir:

- `processed_events`: consumer idempotency kaydi
- `notifications`: UI notification projection side effect'i
- `TicketEventKafkaConsumer`: `ticket.created` eventini okur
- `ConsumerIdempotencyService`: duplicate eventlerde side effect'i atlar

## Sonuc

Duplicate `ticket.created` delivery tek notification kaydi uretir. Pattern,
ileride reporting projection, e-posta delivery ve SLA consumer'lari icin ayni
sekilde uygulanacaktir.
