# ADR-0031: Structured JSON Logging ve Correlation ID

## Karar

#45 icin tum backend servisleri Log4j2 uzerinden tek satir JSON log uretecek.
Zorunlu log alanlari:

- `timestamp`
- `level`
- `serviceName`
- `traceId`
- `spanId`
- `correlationId`
- `message`
- `exception`

HTTP girislerinde `X-Correlation-Id` header'i okunur. Header yoksa servis UUID
uretir, response header'ina ayni degeri yazar ve Log4j2 `ThreadContext`
icindeki `correlationId` alanina koyar. Header degeri yalnizca
`[A-Za-z0-9._:-]` karakterlerini ve en fazla 160 karakteri destekler; guvensiz
degerler log injection riskine karsi yenisiyle degistirilir.

## Neden

OpenSearch tarafinda arama, filtreleme ve dashboard uretimi icin plain text log
yerine stabil JSON alanlari gerekir. Correlation ID butun servislerde ayni
header ve ayni MDC anahtariyla tutulursa gateway, downstream servisler ve hata
response'lari ayni request uzerinden izlenebilir.

## Sonuc

Backend servisleri ayni JSON pattern'i kullanir. `serviceName` her servisin
kendi Log4j2 configinde statik olarak tutulur; boylece log formatinin dogru
calismasi runtime property cozumune bagli kalmaz. `traceId` ve `spanId`
alanlari simdilik MDC'den okunur; OpenTelemetry entegrasyonu geldiginde ayni
alanlar trace context ile beslenecektir. Correlation ID filtreleri servlet
servislerde `OncePerRequestFilter`, gateway'de `WebFilter` olarak uygulanir.
