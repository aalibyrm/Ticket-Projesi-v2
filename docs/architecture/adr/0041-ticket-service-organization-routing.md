# ADR-0041: Ticket-Service Organization Routing

## Karar

Kullanici #60 kapsaminda B secenegini secti: department, team, team member,
team lead, ticket topic ve routing rule modeli bu fazda `ticket-service` icinde
DB tabanli tutulacak.

Bu karar ile:

- Organizasyon ve routing tablolari `ticket-service` sahipliginde olur.
- Keycloak kimlik, oturum ve genel rol bilgisini saglamaya devam eder.
- Agent, team lead ve manager gibi is kurali baglamlari ticket-service DB
  modeliyle dogrulanir.
- Customer ticket olustururken ekip veya agent atamasi gondermez; yalniz konu
  secimi yapar.
- Ticket konusu routing rule ile department ve default team'e cozulur.

## Degerlendirilen Secenekler

### Secenek A: Config/enum tabanli routing

Kod veya config icinde sabit department/topic eslesmeleri tutulabilirdi.

Artisi hizli uygulanmasi ve az tablo gerektirmesiydi. Eksisi ise ekip lideri,
team membership ve degisen routing kurallari icin deploy gerektirmesi oldu.
Kullanici kurumsal ve gelistirilebilir bir model istedigi icin bu secenek
reddedildi.

### Secenek B: Ticket-service icinde DB tabanli model

Department, team, member, lead, topic ve routing rule kayitlari ticket-service
DB'sinde tutulur.

Bu secenek secildi. Cunku ticket routing ticket domain'inin parcasidir ve
mevcut `assignedTeamId`, `assigneeId`, customer ownership ve support access
kurallariyla dogrudan iliskilidir. Yeni servis acmadan kurumsal bir veri modeli
kurulur.

### Secenek C: Ayri organization-service/routing-service

Ayrica bir organization-service veya routing-service acilabilirdi.

Bu secenek daha buyuk kurumsal sistemlerde dogru olabilir; ancak bu projede
servis sayisini, internal auth'u, cache/fallback ihtiyacini ve create ticket
bagimliligini buyutur. Bu faz icin overengineering riski nedeniyle ertelendi.

## Neden

Ticket assignment ve attachment authorization zaten ticket sahipligiyle
iliskilidir. Department/team routing de ticket olusturma aninda alinacak domain
kararidir. Bu nedenle veri ve yetki kararini ayni bounded context icinde tutmak
daha dusuk gecikme, daha az servis bagimliligi ve daha sade test stratejisi
saglar.

Ayri servis ihtiyaci daha sonra user directory, HR entegrasyonu, coklu urun
ailesi veya farkli uygulamalarin ayni organizasyon verisini kullanmasi gibi
gercek bir paylasim ihtiyaci ortaya cikarsa tekrar degerlendirilecektir.

## Guvenlik Etkisi

Bu karar OWASP Broken Access Control riskini azaltacak sekilde uygulanmalidir:

- Customer request icinde `assignedTeamId`, `assigneeId` veya department
  override kabul edilmez.
- Agent ticket erisimi kendi membership kayitlariyla sinirlanir.
- Team lead yalniz lideri oldugu ekiplerin ticket'larini yonetir.
- Manager kapsam karari kullanici tarafindan ayrica secildikten sonra
  uygulanir.
- Admin organizasyon ve routing tanimlarini yonetebilir.
- Local/dev header kolayliklari production JWT akisini bypass etmemelidir.

## Sonuc

Sprint 10 yeni organizasyon ve routing kapsamidir. Uygulama su issue sirasi ile
ilerler:

- #61: Department, team, team member ve team lead modeli.
- #62: Ticket topic ve routing rule modeli.
- #63: Agent, team lead ve manager authorization kurallari.
- #64: Web topic secimi ve team-aware assignment ekranlari.
- #65: Reporting/API dokumanlarinda organization routing gorunurlugu.

#56 final smoke ve runbook issue'su bu kapsam tamamlandiktan sonra ele
alinacaktir.

## Acik Kullanici Kararlari

Uygulama baslamadan once kullanicinin secmesi gereken konular:

- 4 department'in isimleri ve kodlari.
- Her department altindaki baslangic ekipleri.
- Team lead rolunun tam yetkisi: sadece assign/transition mi, yoksa ekip ici
  agent atama ve internal note/worklog yonetimi de dahil mi.
- Manager kapsami: tum sistem mi, sadece departman bazli mi.
