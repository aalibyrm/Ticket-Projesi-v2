# ADR-0001: Dengeli Mikroservis Yapisi

## Durum

Sistem ticket yonetimi, dosya yukleme, SLA/workflow, notification/e-posta,
raporlama, kimlik yonetimi ve gozlemlenebilirlik isterlerini birlikte tasir.

## Karar

Dengeli mikroservis yapisi kullanilacak:

- `api-gateway`
- `ticket-service`
- `workflow-sla-service`
- `file-service`
- `notification-service`
- `reporting-service`

## Neden

Moduler monolit daha hizli baslar ama mikroservis isterini zayif karsilar. Cok
ince taneli servis yapisi ise product, comment, worklog gibi alt alanlari da
ayirarak gereksiz dagitik sistem karmasikligi uretir.

## Sonuc

Servis sinirlari anlasilir kalir. Gelecekte servis bazli olcekleme ve deploy
kolaylasir. Ilk gelistirme maliyeti monolite gore daha yuksektir.

