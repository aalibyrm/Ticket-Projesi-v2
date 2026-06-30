# ADR-0066: Lead Assignment and Payment Team Split

## Status

Accepted

## Context

Agent ekraninda ticket atama yetkisi genis gorunuyordu. Backend tarafinda DB
kaynakli ekip uyeligi zaten yetki kaynagi olsa da UI, lead olmayan agent'a baska
agent secme alanini gosteriyordu. Bu yaniltici deneyim Broken Access Control
beklentisini de belirsizlestiriyordu.

Finance Operations icin odeme operasyonu tek ekip olarak kalmisti. Kullanici,
Finance Operations altinda iki ayri odeme takimi ve bu takimlar icin gercekci
agent/lead isimleri ile SLA rapor verisi istedi.

## Decision

Kullanici #96 kapsaminda su karari verdi:

- Sadece team lead baska agent'a assignment yapabilir.
- Lead olmayan agent sadece bos ticket'i kendi uzerine alabilir.
- Lead olmayan agent UI'da `Agent sec` alanini gormez.
- Finance Operations altinda `PAYMENT_OPERATIONS_1` ve
  `PAYMENT_OPERATIONS_2` ekipleri bulunur.
- Bu ADR ilk kabul edildiginde `PAYMENT_FAILURE` default routing'i
  `PAYMENT_OPERATIONS_1` uzerinde kalmisti. Bu routing karari ADR-0067 ile
  topic bazli round-robin olarak degistirildi.

Lead yetkisi Keycloak claim'inden degil, ticket-service DB'sindeki aktif
`team_members.team_lead` kaydindan okunur. Lead demo kullanicilari Keycloak'ta
`AGENT` rolune sahiptir; assignment yetkisini bu rol tek basina vermez.

## Consequences

- UI ve backend ayni authorization modelini yansitir; normal agent baska agent
  secmeye yonlendirilmez.
- JWT icine genis `team_ids` veya `lead` claim'i koyulmadigi icin yetki kaynagi
  ticket-service DB'sinde kalir.
- Finance Operations raporlari artik Billing, Payment Operations 1 ve Payment
  Operations 2 dagilimini gosterebilir.
- `PAYMENT_FAILURE` routing'i artik ADR-0067 kapsaminda Payment Operations 1 ve
  Payment Operations 2 arasinda round-robin calisir.
