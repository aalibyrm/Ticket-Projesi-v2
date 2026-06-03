# ADR-0047: Mobile Core Style Strategy

## Karar

Kullanici #55 icin A secenegini secti. React Native mobil core ekranlari
Pure React Native `StyleSheet` ve ortak design token yapisi ile uygulanir.

Hazir UI kit, NativeWind veya Tailwind benzeri runtime/className katmani bu
fazda eklenmez.

## Neden

`MobilTasarim` referanslari ozel bir precision minimalist tasarim dili
tanimliyor: duz yuzey, 1px border, golgesiz hiyerarsi, sinirli kirmizi vurgu ve
underline-only inputlar. Hazir component kitleri bu tasarim dili icin yogun
override gerektirir ve gereksiz bagimlilik getirir.

StyleSheet + token yaklasimi:

- Tasarima birebir kontrol verir.
- Expo Managed stack'i sade tutar.
- CI ve runtime bagimlilik yuzeyini kucultur.
- Mobil component tekrar kullanimini `src/components` altindaki primitive
  yapilarla saglar.

## Degerlendirilen Alternatifler

### A. StyleSheet + shared design tokens

Artisi: En az bagimlilik, tasarima en yuksek kontrol, kolay typecheck/build.

Eksisi: Hazir component kit hizini vermez; component primitive'lerini proje
icinde yazmak gerekir.

### B. React Native Paper

Artisi: Hazir form, list, dialog ve button componentleriyle hizli ilerler.

Eksisi: Material hissi `MobilTasarim` dilini bozabilir; cok sayida override ve
theme ayari gerekir.

### C. NativeWind / Tailwind tabanli styling

Artisi: Referans HTML'lerdeki utility sinif mantigina yakindir.

Eksisi: React Native runtime/tooling bagimliligi artar; tasarim token kontrati
class string'lerine dagilabilir.

## Sonuc

#55 kapsaminda mobile core ekranlar `apps/mobile/src/components` primitive'leri
ve `apps/mobile/src/theme/tokens.ts` uzerinden uygulanir. Ticket olusturma
ekranindaki dosya secimi Expo birinci parti `expo-document-picker` moduluyle
baglanir; upload yetki karari file-service presigned URL oncesinde backend
tarafinda kalir.
