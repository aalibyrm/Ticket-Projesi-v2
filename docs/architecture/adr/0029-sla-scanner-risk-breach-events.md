# ADR-0029: SLA Scanner ile Risk ve Breach Eventleri

## Karar

#37 icin A + C1 secenegi secildi: `workflow-sla-service`, SLA state
tablosunu periyodik olarak tarayacak ve priority bazli risk pencerelerine gore
`workflow.sla-risk-detected` ile `workflow.sla-breach-detected` eventlerini
transactional outbox uzerinden yayinlayacak.

Varsayilan risk pencereleri:

- LOW: deadline'dan 12 saat once
- MEDIUM: deadline'dan 4 saat once
- HIGH: deadline'dan 2 saat once

## Degerlendirilen Secenekler

- Scheduled DB scanner + priority bazli risk penceresi: Operasyonel olarak
  sade, test edilmesi kolay ve restart sonrasi DB state uzerinden toparlanir.
  Detection interval kadar gecikme olabilir.
- BPMN/Kogito timer: SLA timer'larini surec diyagraminda gosterir, fakat timer
  persistence, recovery ve job operasyonu bu faz icin daha karmasiktir.

## Neden

Kurumsal kalite icin en onemli ihtiyac kaybolmayan event, idempotent detection
ve izlenebilir state'tir. Scanner + outbox bu ihtiyaci BPMN timer
karmasikligina girmeden karsilar. Kogito ticket lifecycle modelinde kalmaya
devam eder; SLA alarm uretimi workflow-sla-service domain logic'i olarak
tutulur.

## Sonuc

SLA risk ve breach eventleri `workflow.events.v1` topic'ine outbox pattern ile
yayinlanir. Notification-service bu workflow eventlerini ayri consumer ile
dinler ve `SLA_RISK` / `SLA_BREACH` UI notification kayitlari uretir. Payload
minimum tutulur; ticket aciklamasi, yorum, internal note, dosya bilgisi veya
gizli alanlar event'e konmaz.
