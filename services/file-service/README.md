# file-service

Dosya ekleri icin metadata ve ileride eklenecek object storage entegrasyon
servisi.

## Kapsam

Bu fazda servis public upload/download API acmaz. `file_schema` altinda dosya
metadata tablosu, validation status modeli ve application service bootstrap'i
eklenir. Presigned upload/download ve ownership kontrolleri sonraki sprint 03
issue'larinda eklenir.

## Port

Default port: `8082`

## Lokal Calistirma

```powershell
mvn -pl services/file-service spring-boot:run -Dspring-boot.run.profiles=local
```

Health endpoint:

```text
GET http://localhost:8082/actuator/health
```
