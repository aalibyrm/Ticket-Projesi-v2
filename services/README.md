# Services

Backend mikroservisleri bu dizinde tutulur.

- `api-gateway`: Web ve mobil istemciler icin tek giris noktasi.
- `ticket-service`: Ticket, yorum, internal note, worklog, status ve outbox.
- `workflow-sla-service`: BPMN lifecycle, SLA deadline, risk ve breach.
- `file-service`: Cloudflare R2 presigned URL ve dosya metadata.
- `notification-service`: UI notification ve gercek e-posta.
- `reporting-service`: Kafka projection ve manager raporlari.

