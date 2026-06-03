# ADR-0048: Final Smoke Test Strategy

## Status

Accepted

## Context

#56 final smoke kabul kriteri; customer create, agent update,
notification/email, SLA/reporting gorunurlugu ve final runbook dokumantasyonunu
tek bir test edilebilir demo yolu haline getirmeyi gerektirir.

Daha once web frontend icin Playwright + MSW, backend icin ise Spring Boot
integration testleri, Testcontainers, Mailpit ve Kafka/outbox test stratejisi
secilmisti. Bu nedenle #56 yeni bir test framework karari degil, mevcut test
stack'inin son smoke katmanidir.

## Considered Options

### A: Playwright web smoke + browser-level API doubles

React Router dev server explicit `VITE_E2E_AUTH_ENABLED=true` flag'i ile
baslar. Playwright ayrica sayfa yuklenmeden once localStorage'a e2e auth
flag'ini ve rollerini yazar. Auth sadece production olmayan build'de ve explicit
env veya localStorage flag verildiginde lokal test kullanicisi uretir.
Playwright, gateway REST endpointlerini browser network layer'da deterministik
cevaplarla karsilar.

Pros:

- CI'da Keycloak, Kafka, PostgreSQL ve tum servisleri ayaga kaldirma maliyeti
  olmadan kritik frontend yolunu test eder.
- Customer, agent ve manager ekranlari tek tarayici akisiyle dogrulanir.
- Test verisi deterministik oldugu icin hizli ve bakimi kolaydir.

Cons:

- Servisler arasi gercek network, JWT ve DB entegrasyonunu bu test kanitlamaz.
- API mock'lari backend contract'lari degisirse guncellenmelidir.

### B: Docker Compose full-stack black-box smoke

Compose local altyapi, tum Spring Boot servisleri, Keycloak realm, Mailpit ve
web uygulamasi birlikte baslatilir; Playwright gercek gateway uzerinden akar.

Pros:

- En yuksek gerceklik seviyesi.
- Auth, servisler arasi event, e-posta ve reporting read model gecikmeleri ayni
  demo icinde gorulur.

Cons:

- CI suresi ve kaynak tuketimi yuksektir.
- Windows/local ortamda port, Docker ve JVM surec yonetimi daha kirilgan olur.
- Her frontend commit'inde multi-service smoke calistirmak overengineering'e
  kayar.

### C: Sadece manuel runbook

Smoke yolu dokumante edilir, otomasyon eklenmez.

Pros:

- En hizli uygulanir.
- Ek test bakimi yoktur.

Cons:

- Regression yakalama gucu dusuktur.
- #56 kabul kriterindeki "testable" hedefi zayif kalir.

## Decision

User daha once frontend test stack'i icin Playwright + MSW kararini verdigi icin
#56'da A uygulanir. Backend servislerindeki gercek entegrasyon guvencesi mevcut
Maven testleriyle korunur; final runbook ise full-stack manuel demo yolunu
dokumante eder.

## Consequences

- `apps/web` icinde `npm run e2e` kritik smoke yolunu calistirir.
- GitHub Actions frontend job'u `npm run e2e:ci --if-present` ile web smoke'u
  kalite kapisina alir.
- E2E auth bypass production build'de aktif olmaz; sadece production olmayan
  dev/test bundle'inda ve explicit env veya Playwright localStorage flag'i
  verildiginde calisir.
- Ileride deployment pipeline olusursa B secenegi ayri bir system smoke issue'su
  olarak eklenebilir.

## Security Notes

- E2E token lokal ve sahte degerdir; backend authorization testi degildir.
- Backend Broken Access Control kararinin kaynagi degismez: gateway ve servis
  authorization testleri Maven suite icinde kalir.
- Mock'lar hassas secret veya gercek provider credential'i icermez.
