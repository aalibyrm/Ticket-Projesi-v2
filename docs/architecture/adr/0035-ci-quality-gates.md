# ADR-0035: GitHub Actions Kalite Kapilari

## Karar

#49 icin PR ve `main` push'larinda calisan GitHub Actions workflow'u eklenecek.
Workflow backend testleri, Compose config dogrulamasi, frontend kontrolleri,
basic secret scan ve PR dependency review adimlarini ayri job'larda calistirir.

## Neden

Mikroservis monorepo'sunda tek bir uzun job yerine ayrik job'lar daha okunur ve
hata kaynagini daha hizli gosterir. Maven dependency cache backend surelerini
azaltir. Frontend klasorleri henuz scaffold edilmedigi icin frontend job'u
manifest varsa calisir, yoksa deterministik olarak skip eder.

## Sonuc

PR'lar tekrar edilebilir kalite kapilarindan gecer. Basic secret scan ve
dependency review OWASP odakli erken uyari saglar; daha kapsamli SAST/DAST
kurallari ileride ayri issue ile genisletilebilir.
