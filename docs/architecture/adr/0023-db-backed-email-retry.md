# ADR-0023: DB Tabanli E-posta Retry

## Karar

E-posta retry ve delivery state yonetimi `notification-service` icindeki
`email_deliveries` tablosu uzerinden yapilacak.

## Degerlendirilen Secenekler

- DB tabanli scheduled retry: Mevcut delivery tablosu kaynak olur, servis due
  kayitlari belirli aralikla claim edip gonderir.
- Kafka retry topic ve DLQ: Daha yuksek hacimli event-driven retry saglar,
  fakat delay topic, DLQ ve idempotency yonetimini buyutur.
- Harici scheduler: Quartz veya Temporal benzeri bir platform retry'i yonetir,
  fakat operasyonel bagimlilik ekler.

## Neden

Bu asamada e-posta delivery kaydi zaten `notification-service` sahipliginde.
DB tabanli retry, mikroservis sinirini bozmadan duplicate e-posta riskini unique
index ile cozer ve Kafka retry topic karmasasi eklemez.

## Sonuc

Delivery durumlari `PENDING`, `RETRYING`, `SENT`, `FAILED` olarak izlenir.
Duplicate e-postalar `source_event_id`, `template_key` ve normalize edilmis
`recipient_email` uzerinden DB unique index ile engellenir. SMTP gonderimi DB
transaction'i disinda yapilir; claim ve sonuc yazma islemleri kisa transaction
olarak tutulur.
