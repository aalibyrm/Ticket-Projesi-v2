# ADR-0036: Web Design Source Of Truth

## Karar

#58 icin `FrontendTasarim` klasoru ve `docs/design` altindaki dokumanlar web
frontend tasariminin resmi kaynaklari olarak kabul edilir. React implementasyonu
baslamadan once tasarim sistemi, ekran envanteri, role-aware navigation ve
component envanteri dokumante edilir.

## Neden

#50 frontend scaffold isine tasarim netlesmeden baslamak route, layout ve
component kararlarini rastgele hale getirirdi. Verilen tasarimlar kurumsal,
yogun bilgi gosteren ve role-aware bir ticket management deneyimi tarif ediyor.
Bu kararlar repo icinde versiyonlanirsa frontend implementasyonu ayni gorsel
dil ve ekran sozlesmesi uzerinden ilerler.

## Sonuc

Frontend tech stack secimi bu dokumanlardan sonra ayrica yapilir. `#50` ve
sonraki web issue'lari `docs/design` dokumanlarini ve `FrontendTasarim`
artefact'larini referans alir. UI role-based gorunurluk saglar; authorization
kararinin kaynagi backend servisleri olmaya devam eder.
