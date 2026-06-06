# ADR-0056: Internal Note Team Read Scope

## Status

Accepted

## Context

Agent kendi destek kuyruğuna gelen ticket'i okuyabiliyor, ancak ticket'i uzerine
almadan internal note ekleyemiyordu. Internal note musteriye gosterilmez ve
external comment event'i uretmez; bu nedenle musteri ile resmi iletisimden daha
dusuk riskli, support-only bir aksiyondur.

## Decision

Internal note ekleme yetkisi assignment zorunlulugundan ayrilir. Aktif support
team uyesi veya team lead, zaten okuyabildigi kendi team ticket'ina atama
yapmadan internal note ekleyebilir.

External customer reply, status update ve worklog aksiyonlari assigned agent,
team lead veya admin yetkisinde kalir.

## Consequences

- Agent ticket'i claim etmeden once ekip ici analiz notu birakabilir.
- Customer-visible mesaj ve operasyonel state degisikligi daha dar manage
  policy'siyle korunur.
- Cross-team internal note yazimi engellenir; DB-authoritative team membership
  siniri korunur.
