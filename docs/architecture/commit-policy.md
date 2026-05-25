# Commit Policy

## Kural

Her implementasyon commit mesajinda ilgili GitHub issue numarasi bulunmalidir.

Dogru ornekler:

- `docs: add architecture decision records #1`
- `infra: add docker compose foundation #3`
- `ticket-service: add ticket aggregate persistence #7`

Yanlis ornekler:

- `initial commit`
- `fix stuff`
- `ticket changes`

## Neden

Bu proje bilincli mimari kararlarla ilerleyecek. Issue numarasinin commit
mesajinda bulunmasi, hangi karar veya gereksinim icin hangi kodun yazildigini
geri izlenebilir hale getirir.

## Uygulama

- Her is once GitHub issue olarak acilir.
- Commit kapsamı tek issue ile uyumlu tutulur.
- Bir commit birden fazla issue'yu etkiliyorsa mesajda tum issue numaralari
  yazilir.
- Mimari karar degisikligi varsa ilgili ADR guncellenir veya yeni ADR eklenir.

