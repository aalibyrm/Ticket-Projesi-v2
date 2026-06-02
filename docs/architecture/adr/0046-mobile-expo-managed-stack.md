# ADR-0046: Mobile Expo Managed Stack

## Karar

Kullanici A secenegini secti. `apps/mobile` Expo Managed + React Native +
TypeScript olarak scaffold edilir.

Temel mobil stack:

- Expo Managed SDK 56
- React Native 0.85
- React Navigation 7
- Expo AuthSession ile Keycloak OIDC Authorization Code + PKCE
- Expo SecureStore ile token saklama
- Axios ile gateway REST cagrilari
- TypeScript ve Vitest kalite kontrolleri

## Neden

Mobil kapsam ADR-0008'e gore temel ticket akislariyla sinirli baslayacak. Expo
Managed bu kapsam icin native Android/iOS proje bakim maliyetini azaltir,
kurulumu hizlandirir ve kamera/dosya/auth gibi mobil yetenekleri Expo
modulleriyle kontrollu sekilde eklemeye izin verir.

Kullanici kurumsal proje istedigini, ancak overengineering istemedigini belirtti.
Bu nedenle React Native CLI bare proje yerine Expo Managed secildi.

## Degerlendirilen Alternatifler

### A. Expo Managed + React Native + TypeScript

Artisi: En hizli scaffold, sade CI, Expo AuthSession/SecureStore/file picker
gibi modullerle dusuk operasyon maliyeti.

Eksisi: Cok ozel native SDK ihtiyacinda prebuild/eject gerekebilir.

### B. React Native CLI Bare + TypeScript

Artisi: Android/iOS native projelerde tam kontrol.

Eksisi: Ilk kurulum, CI, signing ve native dependency bakimi bu faz icin
gereksiz sekilde agir.

### C. Expo Prebuild / Development Build

Artisi: Expo hizi ile native esneklik arasinda denge.

Eksisi: A secenegine gore daha fazla platform dosyasi ve bakim yuku getirir.

## Sonuc

#54 kapsaminda mobil app gercek auth/navigation/API temeliyle baslar. Mobil
istemci authorization karari vermez; gateway ve backend servislerdeki
authorization authoritative kalir. Ozel native modul ihtiyaci dogarsa yeni issue
altinda Expo Prebuild secenegi tekrar degerlendirilir.
