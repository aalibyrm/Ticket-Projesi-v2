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
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3001`

## Servisleri Tek Komutla Trace ve JSON Log ile Calistir

Altyapi ayaga kalktiktan sonra tum Java servislerini OpenTelemetry Java Agent
ile baslatmak icin repo kokunden asagidaki script calistirilir:

```powershell
.\scripts\start-observability-services.ps1 -Restart
```

Script su isleri yapar:

- `.env` degerlerini process ortamina yukler.
- OpenTelemetry Java Agent yoksa Maven ile indirir.
- `libs/event-contract` local snapshot'ini gunceller.
- `api-gateway`, `ticket-service`, `file-service`, `workflow-sla-service`,
  `notification-service` ve `reporting-service` icin dogru `OTEL_SERVICE_NAME`
  ile ayri JVM process'i baslatir.
- Servis loglarini `logs/<service>.otel.out` ve `logs/<service>.otel.err`
  dosyalarina yazar.

Gercek Keycloak login akisini test ederken profile parametresi verilmez. Bu
durumda servisler varsayilan profil ve `.env` ayarlariyla calisir; JWT resource
server kontrolleri acik kalir. `local` profili yalniz header tabanli erken
gelistirme denemeleri icindir ve UI'nin Bearer token akisini kullanmaz. Bu
mod gerekiyorsa script acikca `-SpringProfile local` ile calistirilir.

Jaeger UI'da sadece 2-3 servis gorunuyorsa genellikle servislerin bir kismi
OpenTelemetry agent olmadan baslatilmistir veya o servise henuz trafik
gitmemistir. Bu durumda script `-Restart` ile tekrar calistirilir, ardindan
uygulamada ticket olusturma, mesaj yazma, status degistirme ve rapor ekranini
acma gibi trafik uretilir.

## Manuel Servis Baslatma Fallback'i

Java Agent jar'i repo'ya commit'lenmez. Script bunu otomatik indirir. Manuel
baslatma gerekiyorsa lokal makinede asagidaki Maven komutu ile indirilebilir:

```powershell
mvn -q org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy "-Dartifact=io.opentelemetry.javaagent:opentelemetry-javaagent:2.12.0" "-DoutputDirectory=infra/observability/agent" "-Dmdep.stripVersion=true"
```

Her servis ayri terminalde repo kokunden calistirilir. Ortak agent ve config
path'i terminale tanimlanir, `OTEL_SERVICE_NAME` ise calistirilan servise gore
degisir. Servisler JSON loglari hem stdout'a hem de `logs/<service>.json.log`
dosyasina yazar.

Paylasilan event contract degistiyse, servisleri tek tek `spring-boot:run` ile
calistirmadan once local snapshot jar'i guncelle:

```powershell
mvn -q -pl libs/event-contract install
```

Sadece `ticket-service` restart edilecekse raw Maven komutu yerine asagidaki
script tercih edilir. Bu script `.env` dosyasini yukledigi icin servis
`TICKET_DB_URL` default'u olan `localhost:5432` hedefine dusmez:

```powershell
.\scripts\start-ticket-service-local.ps1 -Restart
```

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

## Metrics Takibi

#83 icin kullanici A secenegini secti: Spring Boot Actuator Prometheus
endpointleri Micrometer Prometheus registry ile acilir, Prometheus scrape eder
ve Grafana Prometheus datasource ile dashboard sunar.

Backend servisleri lokal JVM process'i olarak calistigi icin Prometheus
container'i servisleri `host.docker.internal:<port>` uzerinden scrape eder.
Scrape path'i tum servislerde aynidir:

```text
/actuator/prometheus
```

Prometheus hedefleri:

- `host.docker.internal:8088` api-gateway
- `host.docker.internal:8081` ticket-service
- `host.docker.internal:8082` file-service
- `host.docker.internal:8083` workflow-sla-service
- `host.docker.internal:8084` notification-service
- `host.docker.internal:8085` reporting-service

Grafana provisioning dosyalari:

- Datasource: `infra/observability/grafana/provisioning/datasources/prometheus.yml`
- Dashboard provider: `infra/observability/grafana/provisioning/dashboards/ticket-metrics.yml`
- Dashboard: `infra/observability/grafana/dashboards/ticket-backend-metrics.json`

Lokal varsayilan Grafana hesabi `.env` uzerinden gelir:

```properties
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
```

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
eklenmez.

`/actuator/prometheus` lokal scrape icin public allowlist'e alinir. Gercek
ortamda Prometheus, Grafana, collector ve OpenSearch endpoint'leri TLS, network
policy ve servis hesabi bazli erisimle sinirlandirilmalidir.
