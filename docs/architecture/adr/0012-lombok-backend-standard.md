# ADR-0012: Lombok Backend Standardi

## Karar

Backend servislerinde Lombok standart olarak kullanilacak. Root Maven parent,
Lombok dependency ve annotation processor konfigurasyonunu tum Spring Boot
servislerine miras birakir.

## Neden

Projenin amaci boilerplate ve angarya kodu azaltmak. Lombok constructor
injection, getter/setter ve kontrollu model siniflarinda bunu saglar.

## Sonuc

Servis, adapter ve configuration siniflarinda `@RequiredArgsConstructor` gibi
anotasyonlarla constructor boilerplate azalir.

JPA entity'lerinde `@Data` kullanilmaz. Bunun nedeni `equals/hashCode/toString`
metotlarinin lazy relation tetiklemesi, hassas veri loglamasi veya domain
kimligi hatasi uretme riskidir. Entity'lerde sadece gereken `@Getter` ve
`@Setter` anotasyonlari kullanilir.

DTO'larda Java `record` oncelikli kalir; Lombok DTO icin ancak record uygun
degilse kullanilir.

