# ADR-0004: Keycloak OAuth2/OIDC Kimlik Yonetimi

## Karar

Kimlik ve yetkilendirme Keycloak ile yonetilecek. Uygulamalar OAuth2/OIDC ve JWT
tabanli calisacak.

## Neden

Custom auth yazmak guvenlik riskini ve bakim yukunu artirir. Keycloak; rol,
client, SSO, OTP ve token yonetimi icin olgun bir cozumdur.

## Sonuc

Roller `CUSTOMER`, `AGENT`, `MANAGER`, `ADMIN` olarak baslayacak. Servisler
Spring Security Resource Server olarak token dogrulayacak. Customer sadece kendi
ticket'larini gorecek; manager rapor ve SLA ekranlarina erisecek.

