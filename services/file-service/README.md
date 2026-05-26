# file-service

Dosya ekleri icin metadata ve ileride eklenecek object storage entegrasyon
servisi.

## Kapsam

Bu fazda servis public upload/download API acmaz. `file_schema` altinda dosya
metadata tablosu, validation status modeli ve application service bootstrap'i
eklenir. Presigned upload/download ve ownership kontrolleri sonraki sprint 03
issue'larinda eklenir.

## Cloudflare R2

R2 adapter S3-compatible AWS SDK v2 presigner kullanir. Credential degerleri
yalnizca server-side environment property'lerinden okunur; client'a access key
veya secret donulmez. Lokal calismada adapter varsayilan olarak kapali gelir.

```properties
R2_ENABLED=false
R2_ENDPOINT=https://<account-id>.r2.cloudflarestorage.com
R2_REGION=auto
R2_BUCKET=ticket-attachments-private
R2_ACCESS_KEY_ID=replace-with-server-side-access-key
R2_SECRET_ACCESS_KEY=replace-with-server-side-secret
```

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
