# ADR-0007: Cloudflare R2 Object Storage

## Karar

Ticket ekleri ve log dosyalari Cloudflare R2 private bucket icinde saklanacak.
Backend kisa sureli presigned upload/download URL uretir.

## Neden

Dosyalari PostgreSQL icinde saklamak buyuk dosya, backup ve performans acisindan
zayiftir. Local volume ise olceklenebilir ve prod'a yakin degildir. R2,
S3-compatible object storage yaklasimini projeye tasir.

## Sonuc

Bucket public olmayacak. Credential client'a verilmez. Object key UUID tabanli
olur. Dosya tipi, boyutu, MIME ve log keyword validasyonu backend tarafinda
yapilir.

