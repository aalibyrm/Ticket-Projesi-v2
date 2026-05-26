# ADR-0013: Gateway Role Matrix

## Karar

API Gateway, JWT dogrulamaya ek olarak route bazli rol matrisi uygular:

| Route | Roller |
| --- | --- |
| `/api/tickets/**` | `CUSTOMER`, `ADMIN` |
| `/api/agent/tickets/**` | `AGENT`, `ADMIN` |
| `/api/workflows/**` | `AGENT`, `ADMIN` |
| `/api/reports/**` | `MANAGER`, `ADMIN` |
| `/api/sla/**` | `MANAGER`, `ADMIN` |
| `/api/products/**` | `CUSTOMER`, `AGENT`, `MANAGER`, `ADMIN` |
| `/api/files/**`, `/api/notifications/**` | Authenticated user |

## Neden

Gateway tek guvenlik siniri degildir, ancak istemciden gelen isteklerde ilk
yetki filtresi olmasi gerekir. Bu karar, yanlis frontend route kullanimi veya
ileride eklenecek servis endpointlerinin kazara genis role acilmasi riskini
azaltir.

## Alternatifler

- Sadece servis icinde authorization: Servis siniri guclu olur, ancak gateway
  her authenticated istegi downstream'e tasidigi icin gereksiz trafik ve daha
  gec hata donusu olusur.
- Sadece gateway authorization: Gelistirmesi hizlidir, ancak gateway bypass
  edilirse Broken Access Control riski dogar.
- Gateway + servis authorization: Ek test ve konfig maliyeti vardir, ancak
  least privilege ve defense-in-depth icin tercih edilen yaklasimdir.

## Sonuc

Bu projede gateway route matrisi erken filtre olarak calisir. Servisler kendi
domain authorization kontrollerini yine uygulamaya devam eder. Customer ticket
ownership kontrolu ticket-service icinde kalir; agent, SLA ve reporting servis
endpointleri eklendikce servis tarafinda da ayni roller test edilir.
