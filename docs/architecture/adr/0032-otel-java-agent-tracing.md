# ADR-0032: OpenTelemetry Java Agent ile Trace Entegrasyonu

## Karar

#46 icin backend servisleri OpenTelemetry Java Agent ile enstrumante edilecek.
Servis koduna OTel SDK bagimliligi eklenmeyecek. Java Agent HTTP server/client,
Spring WebFlux/WebMVC, Kafka client ve JDBC span'lerini otomatik uretir.

Trace toplama akisi:

1. Java servisleri OTLP gRPC ile `localhost:4317` collector endpoint'ine span
   gonderir.
2. OpenTelemetry Collector span'leri batch'ler ve Jaeger'a export eder.
3. Lokal trace incelemesi `http://localhost:16686` Jaeger UI uzerinden yapilir.

## Neden

Mikroservislerde tracing cross-cutting bir konudur. Java Agent yaklasimi servis
kodunu observability SDK'larina baglamadan HTTP, Kafka ve DB span'lerini kapsar.
Bu sayede her servise ayrica dependency, bean veya interceptor ekleme maliyeti
olmaz. Micrometer/manuel SDK secenekleri daha fazla kontrol verir ama bu fazdaki
kabul kriteri icin gereksiz kod ve bakim maliyeti dogurur.

## Sonuc

Lokal ortamda collector + Jaeger ayaga kalkar. Her servis kendi process'inde
`OTEL_SERVICE_NAME` ile calisir; ortak agent ayarlari
`infra/observability/opentelemetry-javaagent.properties` dosyasindan okunur.
Kafka ve DB span'leri otomatik enstrumantasyona dayanir. Hassas veri riskini
azaltmak icin DB statement sanitizer acik tutulur ve header capture
varsayilan kapali davranista birakilir.
