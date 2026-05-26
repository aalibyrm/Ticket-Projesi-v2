# file-service

Dosya ekleri icin metadata ve ileride eklenecek object storage entegrasyon
servisi.

## Kapsam

Servis `file_schema` altinda dosya metadata tablosu, validation status modeli,
presigned upload URL, presigned download URL ve completion akisini yonetir.
Dosya validasyon kurallari sonraki sprint 03 issue'larinda eklenir.

Upload akisi artik iki adimlidir:

1. `POST /api/files/uploads` kisa sureli presigned PUT URL ve UUID tabanli
   object key uretir.
2. `POST /api/files/uploads/{id}/complete` upload metadata kaydini ayni actor
   icin tamamlanmis isaretler.

Download akisi:

1. `POST /api/files/{id}/download-url` metadata kaydini bulur.
2. Upload tamamlanmadiysa URL uretmeden `FILE_NOT_READY` doner.
3. Ticket ownership dogrulamasi icin `ticket-service` internal endpoint'i
   cagrilir.
4. Yetki varsa kisa sureli presigned GET URL doner.

Ticket erisim yetkisi ticket-service tarafindan belirlenir. Bu fazda customer
sadece kendi ticket'ina, admin tum ticket eklerine erisebilir. Assigned
team/agent erisimi assignment modeli geldiginde ayni internal kontrat uzerinden
genisletilecektir.

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
