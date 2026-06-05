# OpenSearch Dashboards

Bu dizin dashboard kaynak artefact'larini tutar. #47 kapsaminda hedeflenen
dashboard:

- `core-metrics-dashboard.json`

## Neden Saved Object Export Degil

OpenSearch Dashboards saved object exportlari ortam, data view id ve urun
surumune hassastir. Bu fazda veri kontrati ve panel sorgulari repo'da stabil
bir kaynak artefact olarak tutulur. Dashboard UI'da olusturulduktan sonra
export alinip bu dizine eklenebilir.

## Data View

OpenSearch Dashboards icinde asagidaki data view olusturulur:

- Name: `ticket-observability-*`
- Time field: `timestamp`

## Paneller

`core-metrics-dashboard.json` asagidaki panelleri tanimlar:

- API response time: server span'leri uzerinden p95, p50 ve average
  `duration.ms`
- API error rate: 5xx server span sayisinin tum server span sayisina orani
- Request volume: server span count, servis bazli ayrim
- Service health: actuator health snapshot'larinin son durumu
- Application error logs: `ERROR` loglari icin fallback panel

## Beklenen Alanlar

Dashboard ana veri kaynagi normalize edilmis observability indexidir. Minimum
alanlar:

- `@timestamp`
- `timestamp`
- `deployment.environment`
- `deployment_environment`
- `service.name`
- `serviceName`
- `event.kind`
- `event_kind`
- `span.kind`
- `duration.ms`
- `http.response.status_code`
- `service.health.status`
- `log.level`
- `level`
- `correlation.id`
- `correlationId`

## Guvenlik

Dashboard panelleri request body, response body, `Authorization` header, cookie
veya musteri PII alanlarini kullanmaz. Hata orani ve response time hesaplari
toplu metriklerden yapilir.
