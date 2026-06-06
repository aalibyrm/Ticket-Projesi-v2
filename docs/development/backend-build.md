# Backend Build

Backend servisleri Maven monorepo yapisi ile yonetilir.

## Versiyon Kararlari

- Java: 21
- Spring Boot: 3.5.14
- Spring Cloud: 2025.0.2
- Lombok: 1.18.46
- Maven Javadoc Plugin: 3.12.0
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

## Aggregate Javadocs

Root Maven reactor aggregate Javadocs uretir. Komut:

```powershell
mvn -DskipTests javadoc:aggregate
```

Output dizini: `target/reports/apidocs/index.html`.

Javadocs eksik yorum nedeniyle build'i kirmamak icin doclint kapali calisir,
ancak Java 21 API linkleri ve UTF-8 encoding merkezi parent POM'da tanimlidir.

## Log4j2

Spring Boot varsayilan olarak Logback kullanir. Bu projede dokuman isterleri
nedeniyle backend servislerinde Log4j2 baseline kullanilacaktir. Servisler
uygulama bagimliliklari eklendikce `spring-boot-starter-log4j2` kullanacak ve
varsayilan logging starter dislanacaktir.

## Security Test Suite

Yetkilendirme ve edge security regresyonlari CI tarafindan tekrar edilebilir
sekilde calistirilir. Ana guvenlik paketi su komutla kosulur:

```powershell
mvn -q -pl services/api-gateway,services/ticket-service -Dtest="*Security*Tests,*JwtRealmRoleConverterTests,*CorsConfigTests,*RateLimitConfigTests" test
```

Bu suite 401, 403, Keycloak realm role mapping, customer ownership, manager-only
report access, CORS allowlist ve rate limit davranislarini kapsar.
