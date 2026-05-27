# ADR-0024: Notification E-posta Entegrasyon Testleri

## Karar

Notification e-posta entegrasyon testleri Testcontainers ile izole PostgreSQL
ve Mailpit container'lari uzerinde calisacak.

## Neden

Issue #32 kabul kriteri, notification pipeline'in CI ve lokalde tekrar
edilebilir olmasini istiyor. Lokal makinede onceden acik bir Mailpit'e baglanmak
testleri kirilgan yapar. Testcontainers, SMTP ve Mailpit HTTP API'yi test
senaryosu icinde ayni sekilde baslatir.

## Sonuc

Kafka consumer idempotency, Thymeleaf template rendering, email retry/dedup ve
gercek SMTP -> Mailpit akisi test suite icinde kapsanir. Testler dis SMTP
credential'i veya gercek provider kullanmaz; bu tercih gizli bilgi sizmasi ve
istenmeyen e-posta gonderimi riskini azaltir.
