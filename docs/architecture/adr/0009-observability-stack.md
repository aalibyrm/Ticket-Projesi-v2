# ADR-0009: OpenTelemetry, OpenSearch ve JSON Loglar

## Karar

Servisler Log4j2 JSON log uretecek. Trace ve metrikler OpenTelemetry ile
toplanacak. Log ve dashboard tarafi OpenSearch uzerinden sunulacak.

## Neden

Dokuman request sayisi, response suresi, hata orani, trace/span ve dashboard
bekliyor. Sadece dosya logu bu ihtiyaci karsilamaz.

## Sonuc

Prod'a yakin izlenebilirlik saglanir. API response time, error rate, request
volume ve service health dashboardlari hedeflenir.

