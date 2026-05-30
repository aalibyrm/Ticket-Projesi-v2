# Frontend Stack

Bu dokuman #59 kararinin uygulama notlarini tutar. Resmi mimari karar
`docs/architecture/adr/0037-web-frontend-tech-stack.md` dosyasindadir.

## Secilen Web Stack

- React 18 + TypeScript.
- Material UI v7 ve MUI X Data Grid Community.
- React Router Framework.
- Redux Toolkit + React Redux.
- TanStack Query.
- OpenAPI Generator `typescript-axios`.
- Keycloak JS.
- React Hook Form + Zod.
- Vitest + Testing Library + Playwright + MSW.

## State Sinirlari

Redux store client tarafli state icindir:

- auth/session ozeti,
- kullanici rol bilgisi ve role-aware shell state,
- UI tercihleri,
- global bildirim veya dialog state.

TanStack Query backend kaynakli server-state icindir:

- ticket listeleri ve detaylari,
- attachment metadata,
- notification verileri,
- reporting verileri,
- mutation sonrasi cache invalidation.

Backend verileri kalici Redux slice olarak kopyalanmaz. Bu sinir, cache
tutarsizligini ve duplicate source-of-truth riskini azaltir.

## REST Client

Frontend REST cagrilari OpenAPI Generator `typescript-axios` ile uretilen
client uzerinden yapilir. Uretilen client, ortak Axios konfigurasyonu ile
gateway base URL, correlation header ve auth header davranisini kullanir.

Manuel Axios servisleri yalniz OpenAPI disi gecici endpoint ihtiyaci varsa ve
issue icinde acikca gerekcelendirildiyse eklenir.

## Auth

Keycloak JS, ADR-0004 ile uyumlu olarak OIDC entegrasyonu icin kullanilir.
Frontend route guard kullanici deneyimi saglar; gercek yetki backend servisleri
ve gateway tarafindan uygulanir.

Token ve realm/client ayarlari frontend icin public build-time configuration
olarak tutulur. Secret degerler frontend bundle'a eklenmez.

## Forms And Tables

React Hook Form + Zod, request DTO'lariyla uyumlu form schema'lari icin
kullanilir. MUI X Data Grid Community ticket listeleri ve rapor tablolarinda
baslangic grid standardidir.

MUI X Pro/Premium ozellikleri gerekiyorsa ayri issue ve lisans karari acilir.

## Tests

- Vitest: unit ve hook testleri.
- Testing Library: component davranis testleri.
- Playwright: kritik role-based web akislari icin e2e smoke testleri.
- MSW: testlerde REST endpoint mock'lari ve backend bagimsiz frontend testleri.
