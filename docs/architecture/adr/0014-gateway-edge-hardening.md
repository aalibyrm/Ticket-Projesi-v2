# ADR-0014: Gateway Edge Hardening

## Karar

API Gateway, asagidaki edge guvenlik kontrollerini uygular:

- Explicit CORS allowlist; wildcard origin kabul edilmez.
- API pathleri icin configurable in-memory fixed-window rate limiter.
- Guvenli response headerlari: CSP, HSTS, frame deny, nosniff, referrer policy
  ve permissions policy.

## Neden

Gateway, istemci trafiginin ilk uygulama siniridir. CORS allowlist, browser
tabanli yetkisiz origin riskini azaltir. Rate limiter, basit brute force ve
spam denemelerini downstream servislere ulasmadan keser. Security headerlari
clickjacking, MIME sniffing ve gereksiz browser capability yuzeyini daraltir.

## Alternatifler

- In-memory limiter: Ek altyapi gerektirmez, lokal/demo ortaminda hizli test
  edilir. Birden fazla gateway instance oldugunda limit instance basina
  uygulanir.
- Redis/Bucket4j tabanli limiter: Cluster genelinde tutarli limit verir.
  Redis kurulumu, operasyon maliyeti ve ek hata modu getirir.
- WAF/API management rate limiting: Production icin gucludur, ancak lokal
  gelistirme ve proje demosunda davranisi kodla gostermek zordur.

## Sonuc

Bu fazda Redis eklenmedi. Docker Compose ve lokal demo kapsami tek gateway
instance kabul ettigi icin in-memory limiter yeterlidir. Gateway yatay
olceklendiginde limiter Redis/Bucket4j veya platform rate limiting katmanina
tasinarak ayni property sozlesmesi korunur. Limiter client tarafindan spoof
edilebilen forwarding header'larini varsayilan olarak guvenilir kimlik kabul
etmez.
