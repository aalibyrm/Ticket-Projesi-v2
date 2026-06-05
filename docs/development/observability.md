# Observability

Bu dokuman lokal trace akisini tarif eder. Hedef, gateway'den baslayan bir HTTP
request'in servisler, Kafka eventleri ve DB islemleri uzerinden Jaeger'da
izlenebilmesi ve backend JSON loglarinin OpenSearch uzerinden aranabilmesidir.

## Altyapiyi Baslat

```powershell
Copy-Item .env.example .env
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile dev up -d
```

Trace UI:

- Jaeger: `http://localhost:16686`
- OTel gRPC endpoint: `localhost:4317`
- OTel HTTP endpoint: `localhost:4318`
- OpenSearch Dashboards: `http://localhost:5601`
- OpenSearch log index pattern: `ticket-observability-*`

## Java Agent'i Indir

Java Agent jar'i repo'ya commit'lenmez. Lokal makinede asagidaki Maven komutu
ile indirilir:

```powershell
mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy "-Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.12.0" "-DoutputDirectory=infra/observability/agent" "-Dmdep.stripVersion=true"
```

## Servisleri Trace ve JSON Log ile Calistir

Her servis ayri terminalde repo kokunden calistirilir. Ortak agent ve config
path'i terminale tanimlanir, `OTEL_SERVICE_NAME` ise calistirilan servise gore
degisir. Servisler JSON loglari hem stdout'a hem de `logs/<service>.json.log`
dosyasina yazar.

```powershell
$agent = (Resolve-Path infra/observability/agent/opentelemetry-javaagent.jar).Path
$config = (Resolve-Path infra/observability/opentelemetry-javaagent.properties).Path
$logDir = (Resolve-Path logs).Path
$env:JAVA_TOOL_OPTIONS = "-javaagent:$agent -Dotel.javaagent.configuration-file=$config -Dapp.log.dir=$logDir"
$env:OTEL_SERVICE_NAME = "ticket-service"
mvn -pl services/ticket-service spring-boot:run
```

Servis adlari:

- `api-gateway`
- `ticket-service`
- `file-service`
- `workflow-sla-service`
- `notification-service`
- `reporting-service`

## Merkezi Log Takibi

#72 icin kullanici B secenegini secti: trace OpenTelemetry/Jaeger hattinda
kalir, JSON log shipping Fluent Bit/OpenSearch hattindan yapilir.

`dev` veya `full` compose profili acildiginda `fluent-bit` servisi repo
kokundeki `logs/*.json.log` dosyalarini read-only mount eder ve
`ticket-observability-*` indexlerine yazar.

```powershell
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile dev up -d
docker compose --env-file .env -f infra/docker/docker-compose.yml --profile dev ps
```

OpenSearch Dashboards icinde data view:

- Name: `ticket-observability-*`
- Time field: `timestamp`

Aranabilir temel alanlar:

- `serviceName`
- `level`
- `traceId`
- `spanId`
- `correlationId`
- `message`
- `exception`
- `deployment_environment`
- `service_namespace`
- `event_kind`

Gateway yeni veya sanitize edilmis `X-Correlation-Id` degerini downstream
request header'ina da yazar. Bir request'i takip ederken ayni `correlationId`
degeri gateway ve downstream servis loglarinda aranabilir.

## Beklenen Trace Kapsami

OpenTelemetry Java Agent asagidaki span'leri otomatik uretir:

- HTTP server/client span'leri
- Spring WebFlux/WebMVC request span'leri
- Kafka producer/consumer span'leri
- JDBC ve Hibernate DB span'leri

Jaeger UI'da servis adina gore arama yapildiginda gateway'den downstream
servislere, Kafka eventlerine ve DB islemlerine uzanan trace agaci gorulmelidir.

## OpenSearch Dashboard Artefact'i

Core metrics dashboard kaynak tanimi
`infra/observability/opensearch-dashboards/core-metrics-dashboard.json`
dosyasindadir. Bu artefact API response time, error rate, request volume ve
service health panelleri icin beklenen index alanlarini ve sorgulari tanimlar.

## Guvenlik

Header capture acilmaz; boylece `Authorization`, cookie veya musteri verisi
gibi hassas alanlar trace attribute olarak toplanmaz. DB statement sanitizer
aktif tutulur. Fluent Bit backend JSON log satirlarini tasir; request body,
response body, R2 secret, dosya icerigi veya e-posta icerigi log pipeline'ina
eklenmez. Gercek ortamda collector ve OpenSearch endpoint'leri TLS ve network
policy ile sinirlandirilmalidir.
