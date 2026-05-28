# CI

GitHub Actions kalite kapilari `.github/workflows/quality-gates.yml`
dosyasinda tutulur.

## Tetikleyiciler

- `pull_request`
- `push` to `main`

## Job'lar

| Job | Amac |
| --- | --- |
| `backend` | Java 21 ile `mvn -B -ntp test` calistirir ve Maven cache kullanir |
| `compose` | `local`, `dev` ve `full` Docker Compose profillerini config olarak dogrular |
| `frontend` | `apps/web` ve `apps/mobile` altinda `package.json` varsa npm install, lint, test ve build calistirir |
| `secret-scan` | Private key, GitHub token, Slack token ve AWS access key gibi yuksek riskli pattern'leri arar |
| `dependency-review` | PR dependency diff'lerinde high severity zafiyetleri engeller |

Frontend uygulamalari henuz scaffold edilmediyse `frontend` job'u basarili
sekilde skip eder. Package manifestleri eklendiginde ayni job otomatik olarak
aktif kontrole donusur.

## Guvenlik

CI repo secret degerlerini loglamaz. Secret taramasi yalnizca yuksek riskli
pattern'lere bakar; gercek production secret yonetimi GitHub Secrets veya
deployment platformunun secret manager'i ile yapilmalidir.
