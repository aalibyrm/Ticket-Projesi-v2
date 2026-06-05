# ADR-0053: Fluent Bit ile Merkezi Log Takibi

## Status

Accepted

## Context

#72 kapsaminda OpenTelemetry ve log takip akisi netlestirildi. Projede zaten
Log4j2 ile tek satir JSON log, `X-Correlation-Id`, OpenTelemetry Collector,
Jaeger ve OpenSearch altyapisi bulunuyor. Eksik kalan kisim, backend JSON
loglarinin merkezi olarak aranabilir hale gelmesiydi.

Degerlendirilen secenekler:

- OpenTelemetry Collector `filelog` receiver ile log toplama.
- Fluent Bit ile JSON log shipping.
- Filebeat veya OpenSearch Data Prepper ile ayri log pipeline.

## Decision

Kullanici B secenegini secti: merkezi log takibi Fluent Bit ile yapilacak.

Trace akisi OpenTelemetry Java Agent, OpenTelemetry Collector ve Jaeger
uzerinden devam eder. Log akisi ise Log4j2 JSON log, Fluent Bit ve OpenSearch
olarak ayrilir. Java servisleri lokal gelistirmede host uzerinde Maven ile
calistigi icin JSON stdout korunur ve ayni JSON satirlari `logs/*.json.log`
dosyalarina da yazilir. Fluent Bit bu dosyalari okuyup
`ticket-observability-*` OpenSearch indexlerine gonderir.

OpenTelemetry Java Agent `otel.logs.exporter=none` ayarini korur. Bu projede
trace ve log sorumlulugu bilincli olarak ayrilmistir: trace OTel Collector,
log Fluent Bit tarafindan tasinir.

## Rationale

Fluent Bit log shipping icin hafif ve yaygin bir endustri standardidir.
OpenTelemetry Collector trace icin kalirken log pipeline'inin Fluent Bit'e
ayrilmasi, local gelistirme deneyimini daha deterministik yapar ve OpenSearch
entegrasyonunu sade tutar.

Collector `filelog` receiver tek bilesen avantaji saglasa da Docker Desktop ve
host Maven process'leri arasindaki log path farklari local ortamda daha kirilgan
bir kurulum uretir. Filebeat/Data Prepper ise bu faz icin daha agir operasyonel
maliyet getirir.

## Consequences

`dev` ve `full` compose profilleri OpenSearch saglikli olduktan sonra Fluent
Bit'i baslatir. Backend servisleri repo kokunden calistirildiginda JSON loglar
`logs/<service>.json.log` dosyalarina yazilir ve Fluent Bit tarafindan
OpenSearch'e aktarilir.

Gateway tarafinda uretilen veya sanitize edilen `X-Correlation-Id`, response
header'inin yaninda downstream request header'ina da yazilir. Boylece gateway ve
downstream servis loglari ayni correlation ID ile aranabilir.

Hassas veri capture edilmez. `Authorization`, `Cookie`, request/response body,
R2 secret ve dosya icerigi log pipeline'ina eklenmez. DB statement sanitizer
trace tarafinda acik kalir.
