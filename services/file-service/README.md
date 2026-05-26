# file-service

Dosya ekleri icin metadata ve ileride eklenecek object storage entegrasyon
servisi.

## Kapsam

Servis `file_schema` altinda dosya metadata tablosu, validation status modeli,
presigned upload URL ve completion akisini yonetir. Download URL ve dosya
validasyon kurallari sonraki sprint 03 issue'larinda eklenir.

Upload akisi artik iki adimlidir:

1. `POST /api/files/uploads` kisa sureli presigned PUT URL ve UUID tabanli
   object key uretir.
2. `POST /api/files/uploads/{id}/complete` upload metadata kaydini ayni actor
   icin tamamlanmis isaretler.

Service-to-service ticket ownership dogrulamasi sonraki issue'larda
guclendirilecektir; bu fazda complete islemi upload rezervasyonunu olusturan
actor ile sinirlanir.

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
