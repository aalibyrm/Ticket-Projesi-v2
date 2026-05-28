# ADR-0033: Dashboard Source Artifact Yaklasimi

## Karar

#47 icin OpenSearch dashboardlari import'a hazir saved object yerine kaynak
artefact olarak dokumante edilecek. `infra/observability/opensearch-dashboards`
altinda dashboard panelleri, sorgulari, beklenen alanlar ve guvenlik sinirlari
versiyonlanir.

## Neden

OpenSearch Dashboards export dosyalari data view id, ortam ve surum farklarina
hassastir. Bu asamada collector ve log ingestion topolojisi evrilirken saved
object export'u cabuk kirilir. Kaynak artefact panellerin niyetini ve sorgu
kontratini stabil tutar; gercek UI export'u daha sonra ayni dizine eklenebilir.

## Sonuc

Core metrics dashboard artifact'i API response time, error rate, request volume
ve service health panellerini tanimlar. Dashboard PII veya auth header gibi
hassas alanlara dayanmaz; aggregate span, health ve log alanlarini kullanir.
