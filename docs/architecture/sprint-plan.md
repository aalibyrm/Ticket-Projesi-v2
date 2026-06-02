# Sprint Plan

Sprintler GitHub milestone olarak olusturulmustur. Tarihler iki haftalik fazlar
halinde planlanmistir.

| Sprint | Milestone | Hedef | Due date |
| --- | --- | --- | --- |
| 01 | Temel Altyapi | Compose, PostgreSQL, Kafka, Keycloak, Gateway, ticket-service CRUD temeli | 2026-06-07 |
| 02 | Yetkilendirme | Keycloak realm, JWT validation, rol bazli access control | 2026-06-21 |
| 03 | Dosya Yonetimi | Cloudflare R2, presigned URL, metadata, validation | 2026-07-05 |
| 04 | Eventing ve Outbox | Kafka topics, ticket outbox, event publish/consume, idempotency | 2026-07-19 |
| 05 | Notification ve E-posta | UI notification, gercek e-posta, template, retry, delivery status | 2026-08-02 |
| 06 | Workflow ve SLA | KIE/Kogito BPMN lifecycle, SLA deadline, risk, breach | 2026-08-16 |
| 07 | Raporlama | Projection tablolar, manager raporlari, rapor API testleri | 2026-08-30 |
| 08 | Observability ve DevOps | Log4j2 JSON, OpenTelemetry, OpenSearch, CI/CD | 2026-09-13 |
| 09 | Web ve Mobil | React web, React Native temel ekranlar, E2E smoke | 2026-09-27 |
| 10 | Organizasyon ve Routing | Department, team, team lead, ticket topic routing ve yetki genisletmesi | 2026-10-11 |

## Is Takip Kurali

- Her sprint isleri GitHub issue olarak takip edilir.
- Her commit mesajinda issue numarasi bulunur.
- Sprint disi yeni is cikarsa once issue acilir, sonra commit atilir.
- #56 final smoke ve runbook isi, kullanicinin ek kapsam kararlarindan sonra en
  sonda tamamlanir.
