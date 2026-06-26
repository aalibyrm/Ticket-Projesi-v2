# Scripts

Tekrarlanabilir gelistirme ve bakim scriptleri bu dizinde tutulur.

Scriptler destructive olmamali ve calismadan once ne yaptigini acikca
belirtmelidir.

## start-observability-services.ps1

Tum Java backend servislerini OpenTelemetry Java Agent ile baslatir. Jaeger'da
servislerin eksik gorunmesini engellemek icin her servis dogru
`OTEL_SERVICE_NAME` degeriyle ayri JVM process'i olarak calistirilir.

```powershell
.\scripts\start-observability-services.ps1 -Restart
```
