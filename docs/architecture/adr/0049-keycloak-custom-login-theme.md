# ADR-0049: Keycloak Custom Login Theme

## Karar

Web ve mobil login tasarimi uygulama icinde sifre toplayan bir form olarak
degil, Keycloak custom login theme olarak uygulanir.

React web ve React Native mobil istemciler Authorization Code + PKCE akisini
korur. Kullanici adi, e-posta, sifre, remember-me ve sifre sifirlama islemleri
Keycloak login sayfasinda tamamlanir.

## Neden

Ticket platformunda kimlik dogrulama otoritesi Keycloak'tur. Uygulama
backend'ine `/auth/login` gibi bir endpoint eklemek kullanici sifresini
uygulama katmanina tasir, direct access grant ihtiyaci dogurur ve Broken
Authentication riskini artirir.

Custom theme secimi, tasarim kontrolunu kaybetmeden sifre isleme sorumlulugunu
Keycloak sinirinda tutar. Web ve mobil ayni kurumsal login deneyimini kullanir.

## Alternatifler

- Uygulama ici on-login ekrani: Hizi yuksektir ve React tarafinda kolay
  gelistirilir. Ancak sifre alani sadece gorsel kalirsa kullaniciyi yaniltir;
  gercek sifre girisi yine Keycloak'ta tekrarlanir.
- Backend login endpoint'i: Form birebir uygulama icinde calisir. Buna karsin
  sifre backend'e tasinir, direct access grant acilir ve OAuth/OIDC best
  practice'lerinden uzaklasilir.
- Keycloak custom theme: Tasarim Keycloak login ekranina tasinir. Ek theme
  dosyalari ve Docker mount gerekir, fakat kimlik dogrulama sorumlulugu dogru
  sinirda kalir.

## Sonuc

Secilen yol mikroservis mimarisini monolitik bir auth katmanina cekmez.
Backend servisleri sadece JWT dogrular; frontend ve mobil istemciler sifre
saklamaz veya sifreyi uygulama API'lerine gondermez.

Theme `infra/keycloak/themes/ticket` altinda tutulur ve Docker Compose ile
Keycloak container'ina read-only mount edilir. Realm export'u `loginTheme:
ticket` ayarini tasir. Mevcut Keycloak container'i daha once import edildiyse
local ortamda theme ayarinin gorunmesi icin container yeniden olusturulmalidir.
