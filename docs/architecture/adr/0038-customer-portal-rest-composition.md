# ADR-0038: Customer Portal REST Composition

## Karar

#51 kapsaminda customer web portal akislari backend mikroservislerine REST
uzerinden baglanir:

- Ticket listesi, ticket detayi, ticket olusturma ve customer external comment
  akislari `ticket-service` uzerinden calisir.
- Attachment upload/download akislari `file-service` presigned URL endpointleri
  uzerinden calisir.
- Bildirim listesi ve read state guncellemesi `notification-service` uzerinden
  calisir.
- Frontend server-state cache ve invalidation islemleri TanStack Query ile
  yonetilir; Redux bu verilerin source-of-truth'i olmaz.

Customer external comment endpointleri `ticket-service` icinde tanimlanir:

- `GET /api/tickets/{id}/comments`
- `POST /api/tickets/{id}/comments/external`

## Neden

Customer portal tek bir kullanici yolculugu gibi gorunse de veri sahipligi
servislerde kalmalidir. Ticket sahipligi ve cross-customer authorization karari
ticket-service tarafindan verildigi icin customer comment yazma ve okuma
yetkisi de ticket-service icinde kontrol edilir.

File-service dosya icerigi ve presigned URL sorumlulugunu korur. Notification
verileri notification-service sahipliginde kalir. Frontend bu kaynaklari tek
ekranda birlestirir; backend veritabani veya servis ic mantigini bypass etmez.

## Sonuc

Customer sadece kendi ticket'larini, kendi ticket'larindaki external yorumlari
ve kendi bildirimlerini gorebilir. Internal yorumlar customer response'larina
dahil edilmez. Dosya yukleme/indirme icin frontend erken validation uygular,
ancak gercek dosya yetkisi ve validation file-service ile ticket-service
arasindaki internal authorization akisi uzerinden korunur.

Bu karar #51 icin frontend journey'i tamamlar; daha sonra comment domain'i ayri
bir servise tasinmak istenirse yeni ADR ile tartisilmalidir.
