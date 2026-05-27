# ADR-0011: Gercek E-posta Bildirimleri

## Karar

Notification sistemi sadece UI notification ile sinirli kalmayacak; gercek
e-posta gonderimi de desteklenecek.

## Neden

Prod'a yakin bir ticket sistemi, ticket acildi, status degisti, SLA riski olustu
ve yorum eklendi gibi olaylarda e-posta uretebilmelidir.

## Sonuc

`notification-service` EmailSenderPort arkasinda SMTP/provider adapter kullanir.
Local gelistirmede Mailpit, prod'a yakin profilde SMTP veya transactional mail
provider kullanilir. Template escaping, retry, delivery status ve deduplication
zorunludur.

## Uygulama Notu

E-posta delivery kayitlari rendered HTML body yerine `template_key` ve
`template_model` saklar. Bu tercih gereksiz PII/HTML saklama yuzeyini azaltir;
render ve escaping email adapter katmaninda yapilir.
