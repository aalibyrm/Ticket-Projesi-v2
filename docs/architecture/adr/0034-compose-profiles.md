# ADR-0034: Docker Compose Profilleri

## Karar

#48 icin Docker Compose altyapisi `local`, `dev` ve `full` profillerine
ayrilacak.

- `local`: PostgreSQL, Kafka, Keycloak ve Mailpit.
- `dev`: `local` kapsamina OpenSearch, OpenSearch Dashboards, Jaeger ve
  OpenTelemetry Collector ekler.
- `full`: su an `dev` ile ayni altyapiyi calistirir; ileride app container'lari
  eklendiginde demo profili olarak genisletilir.

## Neden

Tum altyapiyi her seferinde baslatmak gelistirme makinesinde gereksiz kaynak
tuketir. Minimal `local` profil backend gelistirmeyi hizlandirir. Observability
ihtiyacinda `dev` profili ayni compose dosyasini kullanarak ek servisleri
baslatir. `full` profili ise ileride uctan uca demo topolojisine yer birakir.

## Sonuc

Yeni gelistirici `.env.example` dosyasini `.env` olarak kopyalayip profil
secerek altyapiyi baslatabilir. Secret'lar `.env` veya secret manager ile
verilir; repo'ya gercek credential yazilmaz.
