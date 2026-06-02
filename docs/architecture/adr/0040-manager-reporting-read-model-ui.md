# ADR-0040: Manager Reporting Read Model UI

## Karar

#53 kapsaminda manager rapor ekranlari mevcut `reporting-service` read model
API'lari uzerinden calisir:

- `GET /api/reports/tickets/status-distribution`
- `GET /api/reports/tickets/closed`
- `GET /api/reports/agents/performance`
- `GET /api/reports/sla/compliance`

Frontend bu endpointleri REST + TanStack Query ile okur. Redux rapor datasinin
source-of-truth'i olmaz; sadece auth/client state ayrimi korunur. Rapor ekrani
MUI v7, MUI X Data Grid Community ve hafif progress/bar panelleriyle hazirlanir.
Ek grafik kutuphanesi eklenmez.

## Neden

Manager raporlari operasyonel servis DB'lerini veya ticket-service ic state'ini
frontendden birlestirmemelidir. Reporting-service zaten eventlerden beslenen
projection/read model sahibi oldugu icin rapor yetki ve veri kaynagi tek yerde
kalir.

Bu karar mikroservis sinirlarini korur: ticket-service ticket operasyonunu,
workflow-sla-service SLA state'ini, reporting-service ise raporlama read
modelini sahiplenir. Frontend yalnizca manager/admin kullanicilar icin mevcut
rapor sozlesmelerini gorunur hale getirir.

## Sonuc

Manager status dagilimi, kapanan ticket sayilari, agent performansi ve SLA uyum
raporlarini tek ekranda gorur. Kapali ticket raporu frontendde tarih araligi
state'i ile sorgulanir; diger raporlar anlik snapshot olarak kalir.

Ek chart kutuphanesi secilmedigi icin bundle ve bakim maliyeti dusuk kalir. Daha
sonra drill-down, export veya interaktif zaman serisi gerekir ise yeni issue'da
chart/export alternatifi kullanici tarafindan secilerek eklenmelidir.
