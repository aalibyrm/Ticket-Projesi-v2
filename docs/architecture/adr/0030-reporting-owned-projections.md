# ADR-0030: Reporting Sahipli Projection Read Model

## Karar

#39 icin reporting-service kendi `reporting_schema` alani icinde projection read
model tablolarina sahip olacak. Ticket, workflow ve SLA eventleri ilerleyen
issue'larda bu tablolari guncelleyecek; manager rapor API'leri operasyonel
ticket/workflow tablolarini dogrudan okumayacak.

Baslangic projection modeli:

- `ticket_report_projection`: ticket snapshot, status, assignment ve SLA alanlari
- `ticket_status_daily_projection`: status dagilimi icin gunluk aggregate
- `agent_performance_daily_projection`: agent performans metrikleri
- `sla_compliance_daily_projection`: SLA uyum metrikleri
- `processed_events`: reporting consumer idempotency altyapisi

Guncel status distribution raporu `ticket_report_projection` snapshot
tablosundan hesaplanir ve `CLOSED` haricindeki status'leri sabit sirayla,
zero-count dahil olacak sekilde dondurur.

## Degerlendirilen Secenekler

- Ticket-service tablolarindan senkron rapor sorgusu: En hizli baslangictir,
  fakat raporlama sorgulari operasyonel ticket DB semasina baglanir ve servis
  sahipligi zayiflar.
- Reporting-service icinde event-driven projection: Mikroservis sinirini korur,
  rapor sorgularini optimize eder ve manager dashboard yukunu ticket-service'ten
  ayirir. Eventual consistency kabul edilir.
- Ayrik OLAP/warehouse altyapisi: Kurumsal analitik icin gucludur, fakat bu
  fazda ek altyapi ve operasyon maliyeti overengineering olur.

## Neden

Sprint 07 hedefi manager raporlarini kurumsal ama yonetilebilir bir sekilde
hazirlamaktir. Reporting-service projection modeli, ADR-0003 schema izolasyonu
ve ADR-0018 event contract kararlarina uyumludur. Kod yazma maliyeti sorun
olmasa bile OLAP/warehouse bu asamada gereksiz operasyon maliyeti getirir.

## Sonuc

Rapor endpointleri ilerleyen issue'larda reporting-service DB'sindeki read
model tablolarindan beslenecek. Ticket-service ve workflow-sla-service veri
sahipligi korunur; reporting tarafinda kopya read model tutuldugu icin
raporlar event gecikmesi kadar eventual consistent olur. Event payload'lari
hassas veri, dosya icerigi, presigned URL veya internal note tasimayacak.
