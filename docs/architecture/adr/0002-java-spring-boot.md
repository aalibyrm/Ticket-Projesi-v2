# ADR-0002: Java 21 ve Spring Boot 3.x

## Karar

Backend servisleri Java 21 ve Spring Boot 3.5.x ile gelistirilecek. Ilk build
baseline'i Spring Boot 3.5.14 ve Spring Cloud 2025.0.2 olarak sabitlenmistir.

## Neden

Dokuman Java ve Spring Boot 3.x bekliyor. Java 21 guncel LTS tabani saglar.
Spring Boot 3.x; Spring Security, Spring Data JPA, observability ve modern
container tabanli uygulamalar icin guclu ekosistem sunar. Spring Boot 4.x
mevcut olsa da proje isterleri ve Spring Cloud 2025.0.x uyumu nedeniyle 3.5.x
hatti secilmistir.

## Sonuc

Kurumsal standartlara yakin, uzun vadede bakimi daha kolay bir backend zemini
olusur.

Spring Boot 3.5 patch surumu guvenlik ve bagimlilik guncellemeleri icin ileride
kontrollu sekilde yukseltilebilir.
