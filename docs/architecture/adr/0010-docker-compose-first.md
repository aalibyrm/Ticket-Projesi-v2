# ADR-0010: Docker Compose Ilk Hedef

## Karar

Ilk calistirma ve demo ortami Docker Compose ile kurulacak.

## Neden

Kubernetes daha guclu ama ilk teslim icin gereksiz operasyonel yuk getirir.
Compose, backend, frontend, database, Kafka, Keycloak, OpenSearch ve Mailpit gibi
bilesenleri lokal olarak birlikte calistirmayi kolaylastirir.

## Sonuc

Proje hizli sekilde ayaga kaldirilabilir. Kubernetes gecisi sonraki fazlarda
tasarlanabilir.

