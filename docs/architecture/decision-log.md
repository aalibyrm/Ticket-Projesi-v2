# Architecture Decision Log

Bu dosya, projenin temel mimari kararlarini ve nedenlerini ozetler. Detayli
kayitlar `docs/architecture/adr` altindadir.

| ADR | Karar | Neden | Etki |
| --- | --- | --- | --- |
| ADR-0001 | Dengeli mikroservis yapisi | Gercek mikroservis mimarisi gostermek ama projeyi yonetilebilir tutmak | Servis bazli olcekleme kolaylasir, ilk kurulum maliyeti artar |
| ADR-0002 | Java 21 + Spring Boot 3.x | Guncel, kurumsal ve dokuman isterleriyle uyumlu backend standardi | Uzun vadeli bakim ve guvenlik temeli guclenir |
| ADR-0003 | Tek PostgreSQL instance, servis basina ayri schema/user | Veri sahipligini gostermek, ayri DB operasyon yukune girmemek | Least privilege ve servis sinirlari guclenir |
| ADR-0004 | Keycloak + OAuth2/OIDC + JWT | Auth, rol, SSO, OTP ve token yonetimini uygulama icine gommemek | Guvenlik artar, Keycloak kurulumu gerekir |
| ADR-0005 | Kafka + ticket-service icin sade Outbox Pattern | Kritik ticket eventleri kaybolmasin | 2-4 gun ek maliyet, daha guvenilir event akisi |
| ADR-0006 | Apache KIE/Kogito BPMN workflow | jBPM/BPMN beklentisini modern KIE hatti ile karsilamak | Mimari puani artar, workflow entegrasyonu karmasiklasir |
| ADR-0007 | Cloudflare R2 private object storage | Dosyalari DB yerine sektor standardi object storage'da saklamak | Olceklenebilirlik artar, presigned URL guvenligi gerekir |
| ADR-0008 | React web tam, React Native mobil temel | Ana operasyon web'de guclu, mobil isterler temel seviyede karsilansin | Teslim kapsami dengelenir |
| ADR-0009 | OpenTelemetry + OpenSearch + JSON Log4j2 logs | Log, trace, metrik ve dashboard isterlerini karsilamak | Prod'a yakin izlenebilirlik, ek altyapi maliyeti |
| ADR-0010 | Docker Compose ilk hedef | Tum bilesenleri lokal/demo ortaminda calistirmak | Kurulum kolaylasir, Kubernetes sonraki faza kalir |
| ADR-0011 | Gercek e-posta gonderimi | Notification isterini prod'a yakin hale getirmek | SMTP/provider, retry ve template guvenligi gerekir |
| ADR-0012 | Lombok backend standardi | Boilerplate'i azaltmak ve servis kodunu okunur tutmak | Kontrollu kullanim gerekir; JPA entity'lerinde `@Data` yasak |
| ADR-0013 | Gateway route bazli rol matrisi | Yanlis route kullanimi ve fazla downstream erisimini erken engellemek | Defense-in-depth guclenir, servis tarafinda authorization yine zorunlu kalir |
| ADR-0014 | Gateway edge hardening | CORS, rate limit ve security header kontrollerini ilk giris noktasinda uygulamak | In-memory limiter lokal icin yeterli; yatay olcekte Redis/platform limiter gerekir |
| ADR-0015 | Ticket attachment access authority | Dosya erisimi ticket sahipligi karari oldugu icin ticket-service tarafindan belirlenir | File-service presigned URL oncesi internal authorization cagrisi yapar; assigned team kuralina hazir kalir |
| ADR-0016 | File validation rules | Upload URL oncesi metadata filtresi, upload sonrasi backend preview ile log keyword dogrulamasi gerekir | Client beyanina guvenmeden gecersiz dosyalar reddedilir veya validation status ile isaretlenir |
| ADR-0017 | Ticket detail attachment composition | Ticket detail tek response icinde attachment metadata gostermeli, dosya domain'i file-service'te kalmali | Ticket-service file-service internal metadata API'sini okur; DB paylasimi ve object storage coupling olusmaz |
| ADR-0018 | Shared Java event contract | Java-only, compile-time guvenli ve asiri surec maliyeti olmayan event sozlesmesi gerekir | `libs/event-contract` topic, envelope, event type, version ve payload policy kurallarini paylasir |
| ADR-0019 | Ticket lifecycle action model | Eventleri doguran gercek status, assignment, external comment ve worklog aksiyonlari gerekir | Ticket-service sade operasyon modeliyle agent endpointleri ve transactional outbox eventleri uretir |
| ADR-0020 | Consumer idempotency pattern | Kafka at-least-once delivery duplicate side effect uretebilir | Consumer'lar `(event_id, consumer_name)` kaydi ile side effect'i yalnizca ilk delivery icin calistirir |
| ADR-0021 | Eventing test strategy | Outbox, Kafka publish, retry ve consumer idempotency guvencesi birlikte kanitlanmali | Embedded Kafka + PostgreSQL Testcontainers + deterministik mock failure testleri kullanilir |
| ADR-0022 | Thymeleaf e-posta template'leri | HTML/text e-posta icerigi koddan ayrilmali ve guvenli escaping desteklenmeli | Template dosyalari yonetilebilir olur, internal note alanlari render context'inden temizlenir |
| ADR-0023 | DB tabanli e-posta retry | E-posta delivery state notification-service sahipliginde ve duplicate riskini DB ile kontrol etmek yeterli | Kafka retry topic/DLQ karmasasi eklenmeden PENDING, RETRYING, SENT ve FAILED akisi kurulur |
| ADR-0024 | Notification e-posta entegrasyon testleri | SMTP pipeline lokalde ve CI'da dis provider kullanmadan kanitlanmali | Testcontainers PostgreSQL + Mailpit ile tekrar edilebilir test akisi saglanir |
| ADR-0025 | Workflow/SLA Kogito bootstrap | ADR-0006 Kogito karari Spring Boot platformunu bozmadan uygulanmali | Kogito 10.2.0 workflow-sla-service seviyesinde explicit tutulur ve BPMN runtime smoke test ile dogrulanir |
| ADR-0026 | Ticket lifecycle BPMN modeli | Ticket status gecisleri BPMN uzerinde gorunur ve adapter entegrasyonuna hazir olmali | Signal tabanli `ticketLifecycle` process'i izinli gecisleri ve terminal `CLOSED` kararini dokumante eder |
| ADR-0027 | Ticket workflow adapter | Status degisimleri raw enum mutasyonu yerine BPMN uyumlu policy uzerinden gecmeli | `ticket-service` lokal port/adapter ile invalid gecisleri reddeder ve valid gecislerde outbox event uretir |
| ADR-0028 | Config tabanli SLA policy | Priority bazli deadline hesaplamasi hizli ve sade kurulurken state kalici tutulmali | LOW/MEDIUM/HIGH sureleri config'ten okunur, ticket bazli SLA state workflow DB'de saklanir |
| ADR-0029 | SLA scanner ile risk/breach eventleri | BPMN timer karmasikligi yerine idempotent scanner + outbox bu faz icin daha sade ve guvenilir | Priority bazli risk pencereleri config'ten okunur, workflow eventleri notification-service tarafindan UI notification'a cevrilir |
| ADR-0030 | Reporting sahipli projection read model | Manager raporlarini operasyonel servis DB'lerine baglamadan hazirlamak | Reporting-service kendi read modelini tutar; raporlar event gecikmesi kadar eventual consistent olur |
| ADR-0031 | Structured JSON logging ve correlation ID | OpenSearch dashboard ve request takibi icin log alanlari stabil olmali | Tum backend servisleri ayni JSON alanlarini yazar; guvensiz correlation header degerleri yenilenir |
| ADR-0032 | OTel Java Agent tracing | HTTP, Kafka ve DB span'lerini servis koduna SDK baglamadan toplamak | Servisler Java Agent ile OTLP collector'a yazar; local trace incelemesi Jaeger UI uzerinden yapilir |
| ADR-0033 | Dashboard source artifact | OpenSearch exportlari surum ve data view id'lerine hassas oldugu icin panel kontrati stabil tutulmali | Core metrics dashboard panelleri JSON artifact ve runbook olarak versiyonlanir |
| ADR-0034 | Docker Compose profilleri | Minimal gelistirme ile observability dahil stack farkli kaynak ihtiyacina sahip | `local`, `dev` ve `full` profilleri ayni compose dosyasinda tutulur; secret'lar `.env` disinda versiyonlanmaz |
| ADR-0035 | GitHub Actions kalite kapilari | PR'larda backend, frontend, compose ve guvenlik kontrolleri tekrar edilebilir olmali | Maven testleri, compose config, conditional frontend checks, secret scan ve dependency review workflow'a alinir |
