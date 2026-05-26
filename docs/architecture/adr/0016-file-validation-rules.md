# ADR-0016: File Validation Rules

## Karar

File-service iki asamali dosya validasyonu uygular:

1. Presigned upload URL verilmeden once dosya adi, uzanti, MIME ipucu ve boyut
   siniri kontrol edilir.
2. Upload tamamlandiktan sonra text/log dosyalari icin object storage'dan
   sinirli preview okunur ve log keyword kontrolu yapilir.

Varsayilan kurallar:

- Maksimum boyut: 10 MB.
- Izinli uzantilar: `log`, `txt`, `png`, `jpg`, `jpeg`, `pdf`.
- Izinli MIME ipuclari: `text/plain`, `image/png`, `image/jpeg`,
  `application/pdf`.
- Text log keywordleri: `error`, `exception`, `stacktrace`, `traceback`,
  `failed`.

## Neden

Client tarafindan verilen dosya adi, MIME ve boyut bilgisi tek basina guvenilir
degildir; ancak presigned URL verilmeden once ucuz ve guvenli ilk filtre olarak
kullanilabilir. Text log keyword kontrolu ise client beyanina birakilmaz,
backend object storage'dan sinirli preview okuyarak yapar.

## Alternatifler

- Sadece metadata validasyonu: Hizliydi, ancak text log icerigini dogrulamazdi.
- Client'in keyword sonucu gondermesi: Kolaydi, fakat manipule edilebilir.
- Tum dosyayi backend uzerinden proxy upload etmek: En merkezi kontroldu, ancak
  presigned URL mimarisinin performans avantajini ortadan kaldirirdi.

## Sonuc

Gecersiz metadata upload URL uretilmeden 400 response ile reddedilir. Upload
sonrasi text log keyword kontrolu basarisizsa dosya `REJECTED`, object storage
preview okunamazsa `FAILED`, basariliysa `VALIDATED` isaretlenir.
