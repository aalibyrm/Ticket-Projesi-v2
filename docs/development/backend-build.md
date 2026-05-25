# Backend Build

Backend servisleri Maven monorepo yapisi ile yonetilir.

## Versiyon Kararlari

- Java: 21
- Spring Boot: 3.5.14
- Spring Cloud: 2025.0.2
- Lombok: 1.18.46
- Build tool: Maven

Spring Boot 4.x mevcut olsa bile bu projede Spring Boot 3.x secildi. Bunun
nedeni proje isterlerinin Spring Boot 3.x hattini tarif etmesi ve Spring Cloud
2025.0.x hattinin Boot 3.5 ile uyumlu olmasidir.

## Modul Yapisi

Root `pom.xml` tum backend servisleri icin parent ve aggregator gorevi gorur.

```text
services/
  api-gateway/
  ticket-service/
  workflow-sla-service/
  file-service/
  notification-service/
  reporting-service/
```

Her servis kendi `pom.xml` dosyasinda parent olarak root projeyi kullanir.

## Lombok

Backend servislerinde Lombok standart olarak kullanilir.

Kullanim kurallari:

- Constructor injection icin `@RequiredArgsConstructor` tercih edilir.
- Immutable DTO veya value object ihtiyacinda Java `record` onceliklidir.
- JPA entity'lerinde `@Data` kullanilmaz.
- JPA entity'lerinde gerekiyorsa `@Getter` ve `@Setter` kullanilir.
- Lazy relation veya hassas alan barindiran siniflarda otomatik `@ToString`
  dikkatli kullanilir.
- `equals` ve `hashCode` domain semantigi net degilse otomatik uretilmez.

Bu tercih boilerplate'i azaltir ama entity davranisini kontrolsuz hale
getirmemek icin sinirli anotasyon kullanimi zorunludur.

## Log4j2

Spring Boot varsayilan olarak Logback kullanir. Bu projede dokuman isterleri
nedeniyle backend servislerinde Log4j2 baseline kullanilacaktir. Servisler
uygulama bagimliliklari eklendikce `spring-boot-starter-log4j2` kullanacak ve
varsayilan logging starter dislanacaktir.
