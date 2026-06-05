# ADR-0050: MUI Wrapper Design System

## Karar

#69 icin kullanici B secenegini secti: Material UI v7 projede kalir, fakat
default MUI gorsel dili dogrudan ekranlara yayilmaz. Web UI, `FrontendTasarim`
referanslarini source of truth kabul eden `TM Design System` wrapper component
katmani ve merkezi theme override'lari uzerinden uygulanir.

Bu karar asagidaki uygulama kurallarini getirir:

- MUI `Button`, `Chip`, `Paper`, `Tabs`, `TextField`, `IconButton` gibi
  componentler kullanilabilir; fakat ekranlar mumkun oldugunca `TmButton`,
  `TmStatusChip`, `TmSurface`, `TmTabs`, `TmShell` gibi proje componentleri
  uzerinden yazilir.
- Shadow, gradient, blur ve dekoratif elevation kullanilmaz.
- 72px sidebar, 64px topbar, 32px desktop page margin, 8px radius, 1px border
  ve yogun tablo/list satirlari merkezi tokenlardan gelir.
- MUI X Data Grid sadece gercek grid ozellikleri gerektiginde kullanilir.
  Referans ekranlarda ozel satir layout'u gerekiyorsa custom table/list
  componentleri tercih edilir.

## Neden

Kullanici React 18 + Material UI v7 stack'ini daha once secti. Buna karsin
mevcut frontend implementasyonu MUI default spacing, DataGrid davranisi ve
Paper/Card gorunumu nedeniyle `FrontendTasarim` altindaki Precision Minimalist
tasarima yeterince benzemedi.

Tailwind'i projeye ekleyip statik HTML'i dogrudan tasimak hizli olabilirdi,
ancak MUI + Tailwind birlikte uzun vadede iki ayri tasarim sistemi yaratir.
MUI'yi tamamen birakmak da daha once verilen frontend stack kararini gereksiz
sekilde bozar. Wrapper katmani, secilen kurumsal stack'i korurken gorsel
kontrolu proje tasarim tokenlarina tasir.

## Degerlendirilen Alternatifler

- Sadece MUI theme override: Daha az kod degisikligi gerektirir, fakat ekranlar
  dogrudan MUI component API'lerine bagli kaldigi icin DataGrid, Paper ve form
  default'lari tasarimi kolayca bozar.
- Tailwind ile referans HTML'i tasimak: Pixel-perfect hizli baslar, fakat
  mevcut MUI stack'iyle cift stil sistemi olusturur ve bakim maliyetini artirir.
- MUI wrapper katmani: Ilk component yatirimi gerekir, ancak tasarim kaynaklari
  ile React implementasyonu arasinda surdurulebilir bir sozlesme kurar.

## Sonuc

Frontend ekranlari bundan sonra `docs/design`, `FrontendTasarim/*/screen.png`
ve `FrontendTasarim/*/code.html` referanslariyla karsilastirilir. Yeni ekran
ve refactor issue'larinda once ortak `TM Design System` componentleri tercih
edilir; MUI default componentleri yalnizca bu katmanin icinde veya tasarim
uyumu bozulmadiginda kullanilir.

Bu karar auth, route guard veya backend authorization modelini degistirmez.
Frontend role bazli gorunurluk sadece UX icindir; gercek yetki karari gateway
ve backend servislerinde kalmaya devam eder.
