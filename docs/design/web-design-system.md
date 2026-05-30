# Web Design System

Bu dokuman `FrontendTasarim/design_kararlari/DESIGN.md` kararlarini proje icin
resmi web tasarim kaynagi olarak ozetler. Uygulama kodu yazilirken bu dosya,
`FrontendTasarim` altindaki statik HTML ve ekran goruntuleri ile birlikte
referans alinacaktir.

## Design Direction

- Sistem adi: `Precision Minimalist System`
- Stil: Swiss Minimalism, International Typographic Style, macOS esintili
  kurumsal operasyon arayuzu
- Hedef: ticket operasyonlarinda hizli tarama, yuksek bilgi yogunlugu, net
  aksiyon ayrimi
- Yasakli gorsel etkiler: shadow, blur, gradient, dekoratif 3D efekt
- Derinlik modeli: tonal layering ve 1px line work

## Color Tokens

| Token | Deger | Kullanim |
| --- | --- | --- |
| `background` | `#f9f9fe` | Sayfa zemini |
| `surface` | `#f9f9fe` | Ana calisma alani zemini |
| `surface-container-lowest` | `#ffffff` | Panel ve form yuzeyleri |
| `surface-container-low` | `#f3f3f8` | Hover ve ikincil yuzey |
| `surface-container` | `#ededf2` | Chip, passive state |
| `surface-container-high` | `#e8e8ed` | Header ve ayrim yuzeyleri |
| `surface-container-highest` | `#e2e2e7` | Grafik ve ayrim vurgusu |
| `on-surface` | `#1a1c1f` | Ana metin |
| `secondary` | `#5f5e5e` | Metadata, pasif icon, yardimci metin |
| `outline-variant` | `#e4beb7` | Hafif border |
| `primary` | `#7f0900` | Aktif navigation ve kritik vurgu |
| `primary-container` | `#aa1101` | Primary aksiyon, urgency, aktif state |
| `error` | `#ba1a1a` | Hata ve kritik durum |

`primary` ve `primary-container` yogun kullanilmamalidir. Bu renkler ana aksiyon,
aktif navigation, SLA/priority riski ve kritik durum icin ayrilir.

## Typography

| Token | Font | Size | Weight | Line height | Kullanim |
| --- | --- | --- | --- | --- | --- |
| `headline-xl` | Outfit | 32px | 600 | 40px | Sayfa ana basligi, buyuk KPI |
| `headline-lg` | Outfit | 24px | 600 | 32px | Panel ana basligi |
| `headline-md` | Outfit | 20px | 600 | 28px | Kart/panel basligi |
| `headline-sm` | Outfit | 16px | 600 | 24px | Liste item basligi |
| `body-lg` | DM Sans | 16px | 400 | 24px | Uzun okunabilir metin |
| `body-md` | DM Sans | 14px | 400 | 20px | Standart UI metni |
| `body-md-bold` | DM Sans | 14px | 500 | 20px | Liste basligi, buton metni |
| `label-sm` | DM Sans | 12px | 500 | 16px | Form label, tablo header |
| `caption` | DM Sans | 11px | 400 | 14px | Zaman, kisa metadata |

Implementation sirasinda fontlar tek yerden tanimlanmali ve componentler bu
tokenlari kullanmalidir.

## Layout

- Sol sidebar: fixed `72px`, icon-only, aktif state icin sol border/pill.
- Top header: role ve sayfaya gore search, notification, help ve profile
  kontrolleri.
- Ana icerik margin: desktop icin `32px`.
- Grid sistemi: `8px` base unit.
- Paneller: border ile ayrilir; panel icinde shadow kullanilmaz.
- Agent workbench: sol ticket queue, orta conversation/detail, opsiyonel sag
  customer/SLA detail paneli.
- Customer shell: daha sade liste/form odakli, ayni sidebar dili korunur.

Manager raporu referans ekraninda sol ust brand alaninda crop/tasma gorunuyor.
Implementation sirasinda sidebar width ve brand alanlari sabit boyutlarla
duzeltilmelidir.

## Shape And Borders

- Standart radius: `8px`.
- Compact radius: `4px`.
- Panel, card, form control ve button border: `1px solid`.
- Input stili: underline-only; focus durumunda primary renk ve 2px alt border.
- Hover: shadow yerine `surface-container-low` ton gecisi.

## Interaction Rules

- Icon-only buttonlarda `aria-label` ve tooltip zorunludur.
- Primary button sadece ana karar aksiyonunda kullanilir.
- Dangerous action, critical priority ve active navigation ayni red family ile
  ayrilir; ayni ekranda birden fazla primary odak olusturulmamalidir.
- Table/list row hover state layout boyutunu degistirmemelidir.
- Ticket detay ve mesaj ekranlarinda composer sabit tabanda kalmalidir.

## Accessibility And Security

- Role bazli UI gizleme sadece kullanici deneyimi icindir; erisim yetkisi
  backend servislerinde enforce edilir.
- PII, token, Authorization header veya cookie degerleri UI loglarina ve
  analytics eventlerine yazilmaz.
- Dosya ekleri UI'da metadata olarak gosterilir; indirme/yukleme izinleri
  backend presigned URL ve ticket authorization akisiyle dogrulanir.
