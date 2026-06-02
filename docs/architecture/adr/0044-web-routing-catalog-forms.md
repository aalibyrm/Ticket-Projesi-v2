# ADR-0044: Web Routing Catalog Forms

## Karar

Kullanici #64 kapsaminda A secenegini secti: web formlari routing ve
assignment icin backend kataloglarini okur, kullaniciya kontrollu select
secenekleri gosterir.

Customer ticket olusturma formu artik raw routing verisi veya team bilgisi
almaz. Kullanici `ticket-service` tarafindan yayinlanan aktif topic katalogundan
bir `topicCode` secer; backend bu code ile department ve default team route'unu
cozer.

Agent assignment paneli serbest UUID alanlari yerine `ticket-service`
organization katalogundan aktif team listesini ve secili team'in aktif member
listesini kullanir. Assignment mutation yine `assignedTeamId` ve `assigneeId`
gonderir, ancak bu degerler UI tarafinda katalog kontrollu secilir.

## Degerlendirilen Secenekler

### A: Katalog kontrollu select formlari

Secildi. Backend source-of-truth kalir, frontend sadece aktif katalogu gorunur
hale getirir. Kucuk ve orta boy ekip/topic listelerinde hizli, okunur ve test
edilebilir bir cozumdur.

### B: Serbest UUID girisi + backend validation

Kod degisikligi daha az olurdu, ancak operasyonel UI icin zayif kalirdi.
Kullanicinin UUID bilmesini gerektirir, yanlis assignment denemelerini artirir
ve testlerde gercek kullanici akisini temsil etmez.

### C: Search/autocomplete tabanli katalog secimi

Buyuk organizasyonlarda daha iyi arama deneyimi saglar. Bu fazda topic ve ekip
sayisi kontrollu oldugu icin ek state, debounce ve test karmasasi gereksiz
bulundu.

## Guvenlik Etkisi

- Customer formu team, department veya assignee override gondermez.
- Agent panelinde secenekler backend katalogundan gelir; frontend yetki
  kaynagi degildir, sadece hatali input riskini azaltir.
- Nihai assignment ve ticket erisim yetkisi yine `ticket-service` tarafinda
  DB authoritative team authorization kurallariyla uygulanir.
- Serbest UUID girisi kaldirildigi icin yanlis actor/team denemeleri ve bilgi
  sizintisi riski azalir.
- Frontend UUID validation kurali Java `UUID` string formatiyla uyumludur.
  `z.uuid()` daha kati RFC variant kontrolu yaptigi icin backend seed ID'lerini
  yanlis reddetmemesi amaciyla canonical hex-hyphen format kontrolu kullanilir.

## Sonuc

#64 web akisi customer tarafinda `topicCode` tabanli ticket create, agent
tarafinda ise team/member katalogu tabanli assignment kontrolu kullanir.
TanStack Query bu kataloglari server-state olarak cache'ler; Redux'a backend
katalog verisi kopyalanmaz.
