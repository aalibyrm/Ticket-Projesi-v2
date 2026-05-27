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
