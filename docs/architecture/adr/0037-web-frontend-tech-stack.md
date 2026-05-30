# ADR-0037: Web Frontend Tech Stack

## Karar

#59 icin web frontend ana teknolojileri kullanici karariyla asagidaki gibi
belirlenmistir:

- React 18 + TypeScript.
- Material UI v7 ve MUI X Data Grid Community.
- React Router Framework.
- Component tabanli UI mimarisi.
- Redux Toolkit + React Redux ile client/session/UI state yonetimi.
- TanStack Query ile backend REST server-state, cache ve invalidation yonetimi.
- OpenAPI Generator `typescript-axios` ile backend OpenAPI sozlesmelerinden
  TypeScript Axios client uretimi.
- Keycloak JS ile Keycloak OIDC entegrasyonu.
- React Hook Form + Zod ile form state ve validation.
- Vitest + Testing Library + Playwright + MSW ile test ve mock altyapisi.

## Neden

Kullanici React 18, Material UI v7, TypeScript, React Router Framework, React
Redux, component tabanli yapi ve REST backend ile ilerlemek istedigini belirtti.
Bu karar, backend mikroservislerinin REST sozlesmelerine dogrudan baglanan,
tip guvenli ve kurumsal bir web istemcisi kurmayi hedefler.

Redux Toolkit + TanStack Query ayrimi, client state ile server state'i ayni
store icinde karistirmadan yonetir. Redux auth/session, role-aware shell ve UI
tercihleri gibi client tarafli state icin kalir; TanStack Query ticket,
attachment, notification ve reporting gibi backend kaynakli verilerin cache,
loading, retry ve invalidation davranisini yonetir.

OpenAPI Generator `typescript-axios`, backend DTO'lari ile frontend tipleri
arasindaki kaymayi azaltir. Keycloak JS, ADR-0004'te secilen Keycloak tabanli
auth mimarisiyle ayni urun ailesinde kalir. React Hook Form + Zod, yogun ticket
formlarinda performansli form state ve tip odakli validation saglar. MUI X Data
Grid Community, ticket listeleri ve rapor tablolarinda lisans maliyeti olmadan
baslangic icin yeterli tablo yetenekleri sunar.

## Degerlendirilen Alternatifler

- Redux Toolkit + RTK Query: Tek Redux ekosistemi icinde kalmasi avantajliydi,
  ancak kullanici server state icin TanStack Query ayrimini secti.
- Elle yazilan Axios servis katmani: Basit baslar, fakat OpenAPI tabanli client
  kadar contract guvenligi saglamaz.
- `oidc-client-ts`: Keycloak disi OIDC esnekligi saglar, ancak mevcut backend
  auth kararimiz Keycloak oldugu icin kullanici Keycloak JS secti.
- MUI X Pro/Premium: Gelismis grid ozellikleri saglar, ancak bu fazda lisans
  maliyeti ve overengineering riski olmadan Community surumu yeterli goruldu.
- Yup veya yalniz MUI validation: Daha basit gorunur, ancak Zod TypeScript
  odakli schema ve type uyumu nedeniyle secildi.

## Sonuc

#50 web scaffold isinde `apps/web` bu stack ile baslatilir. Server-state Redux
slice'larina tasinmaz; backend verileri TanStack Query uzerinden okunur ve
mutate edilir. Redux store, client/session/UI state ile sinirli tutulur.

API cagrilari REST uzerinden gateway/backend servislerine gider ve frontend
tarafinda OpenAPI'dan uretilmis TypeScript Axios client kullanilir. Backend
OpenAPI sozlesmesi degistikce frontend client yeniden uretilebilir hale
getirilir.

Frontend route guard ve role-aware UI yalnizca kullanici deneyimi icindir.
Gercek authorization karari backend servislerinde ve gateway katmaninda kalir.
Token ve auth config degerleri public frontend build degiskenleriyle sinirli
tutulur; secret degerler frontend bundle'a gomulmez.

React 18 secimi nedeniyle MUI v7 kurulumunda React 18 uyumlulugu ve `react-is`
surum eslesmesi #50 sirasinda kontrol edilir.
