# ADR-0022: Thymeleaf E-posta Template'leri

## Karar

Transactional e-posta template'leri icin Thymeleaf kullanilacak.

## Degerlendirilen Secenekler

- Java-native renderer: Yeni bagimlilik azdir, fakat HTML/text template'leri kod
  icinde buyur ve bakimi zorlasir.
- Thymeleaf: Spring Boot ekosistemiyle uyumludur, HTML escaping davranisi
  gucludur ve template dosyalari koddan ayrilir.
- FreeMarker: E-posta template'lerinde kullanilabilir, fakat bu projede Spring
  HTML template akisi Thymeleaf kadar dogal degildir.

## Neden

Kurumsal projede e-posta icerikleri zamanla degisecek. Template dosyalarinin
koddan ayrilmasi gelistirme ve inceleme maliyetini dusurur. Thymeleaf HTML
template'lerinde `th:text` ve escaped inline syntax kullanarak kullanici
icerigini guvenli bicimde render etmeyi kolaylastirir.

## Sonuc

`notification-service`, `EmailTemplateRendererPort` arkasinda Thymeleaf adapter
kullanir. Subject, plain text ve HTML ciktilari ayri template dosyalarindan
uretilir. Render modelindeki `internalNotes`, `internalComment`, `worklog` gibi
ic not alanlari render context'inden temizlenir; bu tercih Broken Access Control
ve bilgi sizmasi riskini azaltir.
