# ADR-0015: Ticket Attachment Access Authority

## Karar

Ticket dosya eklerine erisim yetkisi `ticket-service` tarafindan belirlenir.
`file-service`, presigned upload veya download URL uretmeden once
`ticket-service` internal attachment access endpoint'ine sorar.

Ilk uygulamada su kurallar gecerlidir:

- `CUSTOMER` sadece kendi ticket'ina dosya ekleyebilir ve dosya indirebilir.
- `ADMIN` tum ticket eklerine erisebilir.
- `AGENT` erisimi assignment modeliyle birlikte sadece assigned agent veya
  ticket-service DB'sindeki aktif assigned team membership icin acilir.

#63 ve ADR-0043 ile `team_ids` claim/header degerleri authorization kaynagi
olmaktan cikarildi. Support ekip kapsami aktif `team_members` kayitlarindan
cozulur.

## Neden

Dosya metadata'si `file-service` icinde olsa da ticket sahipligi ticket domain
kararidir. Bu karar file-service icine kopyalanirsa Broken Access Control riski
artar ve ileride assigned team/agent kurallari iki farkli yerde guncellenmek
zorunda kalir.

## Alternatifler

- Sadece file-service metadata kontrolu: Daha hizliydi, ancak `uploaderId`
  kontrolu ticket sahipligini garanti etmez.
- Tum agent rollerine dosya erisimi acmak: Kisa vadede kolaydi, ancak assigned
  team kuralini delip fazla yetki verirdi.
- Kafka projection ile ticket access verisini file-service'e tasimak: Uzun
  vadede olceklenebilir, ancak outbox/eventing fazi tamamlanmadan bu issue icin
  gereksiz karmasiklik getirirdi.

## Sonuc

URL uretilmeden once ticket access dogrulamasi yapilir. Bu sayede presigned URL
object storage tarafinda kisa sureli dogrudan erisim verse bile backend once
domain yetkisini kontrol eder. Assignment modeli geldiginde internal endpoint
genisletilecek; file-service kontrati degismeden kalacaktir.
