# ADR-0043: DB Authoritative Team Authorization

## Karar

#63 kapsaminda kullanici A secenegini secti: support ekip uyeligi ve team lead
yetkisi icin `ticket-service` DB kayitlari tek authoritative kaynak olur.
JWT veya local test header icindeki `team_ids` degerleri ticket authorization
karari icin kullanilmaz.

JWT yalniz actor kimligi ve genel roller icin kullanilir:

- `AGENT` ve `TEAM_LEAD` support endpointlerine girebilir.
- `MANAGER` ticket listesini ve okunabilir ticket kapsamlarini read-only gorur.
- `ADMIN` tum ticket operasyonlarini yonetebilir.
- Team membership ve team lead kapsami `team_members` tablosundaki aktif
  kayitlardan cozulur.

## Degerlendirilen Secenekler

### A: DB authoritative membership

Ticket-service her support request icin actor'un aktif ekip uyeliklerini ve
liderlik ettigi ekipleri kendi DB'sinden cozer.

Bu secenek secildi. Cunku ticket sahipligi, routing ve assignment ticket
domain'inin parcasidir. Yetki karari da ayni bounded context icinde kalinca
`team_ids` claim spoof, stale token ve cross-team Broken Access Control riski
azalir.

Trade-off: Her support request ek DB lookup yapar. Buna karsilik authorization
karari guncel DB state'ine dayanir ve Keycloak claim senkronizasyonu zorunlu
olmaz. Ileride performans ihtiyaci dogarsa kisa sureli cache ayrica
degerlendirilir; #63 icin Redis/cache eklenmedi.

### B: JWT + DB birlikte

JWT ekip claim'i ilk filtre, DB ise kritik operasyonlarda ikinci kontrol
olabilirdi.

Avantaji daha az DB lookup gibi gorunmesidir. Dezavantaji iki kaynak arasinda
drift uretmesidir: token expire olana kadar eski ekip yetkisi devam edebilir
ve test/local header spoof riski artar. Bu proje kurumsal Broken Access Control
kontrolunu one aldigi icin secilmedi.

### C: Ayri organization/IAM authorization servisi

Yetki karari ayrica bir organization-service veya policy engine tarafindan
verilebilirdi.

Bu daha buyuk kurumsal yapilarda anlamli olabilir; ancak su an ticket routing,
team membership ve assignment modeli ticket-service icinde tutuluyor. Ayrica
servis acmak latency, deployment ve dagitik tutarlilik maliyeti getirirdi.
Overengineering olmamasi icin secilmedi.

## Uygulama Kurallari

- Customer sadece kendi ticket'ini ve kendi ticket attachment'larini gorebilir.
- Customer assignment, department veya routing override gonderemez.
- Aktif ekip uyesi, kendi ekibine atanmis ticket'i okuyabilir.
- Atanmis agent, kendi ticket'inda status, comment ve worklog operasyonlarini
  yapabilir.
- Team lead, yalniz lideri oldugu aktif ekipteki ticket'lari yonetebilir.
- Team lead assignment yaparken hedef ekip de kendi liderlik ettigi ekip
  olmalidir.
- Manager tum ticket listesini okuyabilir, mutation yapamaz.
- File-service upload/download URL uretmeden once ticket-service internal
  authorization endpointine sorar; support actor team membership yine DB'den
  cozulur.

## Guvenlik Etkisi

Bu karar OWASP Broken Access Control riskini dogrudan azaltir:

- Client veya JWT icindeki `team_ids` degeriyle baska ekibe erisim verilmez.
- Aktif olmayan ekip, department veya team member kaydi yetki dogurmaz.
- Team lead yetkisi genel rol claim'inden degil aktif DB liderlik kaydindan
  gelir.
- Manager read-only tutuldugu icin raporlama rolunun operasyonel mutation
  yapmasi engellenir.

## Sonuc

#63 ile support authorization akisi `SupportActorContextService` uzerinden DB
membership cozecek sekilde guncellendi. Query, command ve attachment access
akislari read/manage/assign ayrimina ayrildi. Security testleri claim spoof,
DB membership, team lead scope ve manager read-only davranisini kapsar.
