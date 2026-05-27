# ADR-0021: Eventing Test Strategy

## Karar

Kafka ve outbox guvenilirligi icin katmanli test stratejisi kullanilir:

- `ticket-service` committed ticket degisikliklerinin outbox'a yazildigini
  PostgreSQL Testcontainers ile dogrular.
- `ticket-service` outbox publisher'in gercek Kafka producer yolunu Embedded
  Kafka ile dogrular.
- Kafka failure ve retry akisi mock `KafkaTemplate` ile deterministik test
  edilir.
- `notification-service` duplicate event delivery durumunda idempotent consumer
  davranisini kendi PostgreSQL Testcontainers testinde dogrular.
- Maven Surefire test JVM'leri `en-US` locale ile baslatilir.

## Neden

Bu seviye, committed ticket degisikligi icin event kaybi olmadigini kanitlamak
icin yeterli guvence verir: domain transaction outbox kaydini uretir, publisher
bu kaydi Kafka'ya gonderir, failure durumunda kayit retry icin korunur ve
consumer duplicate side effect uretmez.

Tam multi-service end-to-end test bu fazda daha yavas ve kirilgan olurdu.
Embedded Kafka, gercek Kafka client/producer yolunu test ederken suite'i lokal
gelistirme icin makul hizda tutar. Failure senaryosunda mock kullanmak broker'i
bilerek bozma ihtiyacini ortadan kaldirir ve retry sonucunu deterministik yapar.

`en-US` locale sabitlemesi Kafka test broker'inin locale-sensitive enum
donusumlerinde Turkce `i` karakterinden etkilenmesini engeller.

## Sonuc

Eventing testleri kurumsal guvenilirlik seviyesine yaklasir, ancak henuz tum
servislerin ayni anda ayaga kalktigi Compose tabanli smoke test degildir. Bu
ayrim testleri hizli ve bakimi kolay tutar; ileride deployment fazinda ek
system smoke testleri eklenebilir.
