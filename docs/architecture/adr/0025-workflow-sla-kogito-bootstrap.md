# ADR-0025: Workflow/SLA Kogito Bootstrap

## Karar

`workflow-sla-service`, Spring Boot 3.5.14 servis iskeleti uzerinde Kogito
10.2.0 BPMN runtime ile baslatilacak. Kogito icin
`org.jbpm:jbpm-with-drools-spring-boot-starter` ve
`org.kie.kogito:kogito-maven-plugin` kullanilacak.

## Degerlendirilen Secenekler

- Kogito BOM'u parent dependency management'e almak: Kogito dependency
  versiyonlarini tek yerden yonetir, fakat root Spring Boot 3.5.14 ve Spring
  Cloud 2025.0.2 platform kararini dolayli olarak etkileyebilir.
- Servis seviyesinde explicit Kogito versiyonu kullanmak: Sadece
  `workflow-sla-service` etkilenir ve diger servislerin dependency management
  cizgisi korunur.
- Kogito yerine sade enum/state machine ile baslamak: Ilk kod daha basit olur,
  fakat ADR-0006 ve BPMN beklentisiyle uyumsuz kalir.

## Neden

ADR-0006 ile BPMN icin KIE/Kogito karari verilmis durumda. Bu issue'da yeni
urun karari degil, bu kararin Spring Boot servis modulu icinde uygulanma sekli
netlestirildi. Kogito versiyonunu servis POM'unda explicit tutmak, Kogito
entegrasyonunun blast radius'unu tek module indirir ve mevcut Spring Boot
platform kararini korur.

## Sonuc

Servis `workflow_schema` Flyway baseline'i ile baslar ve build sirasinda BPMN
asset'lerinden Kogito modeli uretir. `runtimeSmoke` BPMN dosyasi sadece runtime
bootstrap kabul kriterini kanitlayan minimal smoke process'tir; gercek ticket
lifecycle ve SLA modelleri sonraki workflow issue'larinda ayrica eklenecektir.
Spring component scan kapsami, uygulama paketiyle birlikte Kogito codegen'in
urettigi `org.kie.kogito.app` ve starter auth bridge icin
`org.kie.kogito.spring.auth` paketlerini de kapsayacak sekilde explicit tutulur.
Process runtime'in bekledigi `CorrelationService`, Kogito'nun
`DefaultCorrelationService` implementasyonu ile Spring bean olarak saglanir.
Runtime transaction boundary icin Kogito'nun static `UnitOfWorkManager`
implementasyonu kullanilir; persistent workflow store sonraki issue'larda
tasarlanmadikca ek operasyonel bagimlilik eklenmez.
