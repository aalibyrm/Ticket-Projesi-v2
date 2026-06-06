# ADR-0055: Team Member Self-Assignment

## Status

Accepted

## Context

Topic routing ticket'i once default support team'e atar ve `assignee_id` bos
kalabilir. DB-authoritative authorization kararinda aktif team member bu
ticket'i okuyabilir, ancak mesaj, status ve worklog gibi mutasyonlar assigned
agent, team lead veya admin ile sinirlidir.

Bu nedenle regular agent kendi ekibindeki kuyruk ticket'ini gorebildigi halde
`Bana ata` aksiyonu 403 aldiginda ticket'i uzerine alamaz ve operasyonel isleme
baslayamaz.

## Decision

Aktif support team member, yalnizca su kosullarin tamaminda self-assignment
yapabilir:

- Ticket mevcut durumda assignee'sizdir.
- Ticket'in `assigned_team_id` degeri actor'un aktif uyesi oldugu team'dir.
- Request `assignee_id` olarak actor'un kendi id degerini tasir.
- Request ticket'i farkli bir team'e tasimaz.

Admin ve team lead reassignment yetkileri aynen korunur.

## Consequences

- Agent kendi kuyrugundaki bos team ticket'ini claim edip mevcut assigned-agent
  kuralindan gelen status, external comment, internal note ve worklog
  aksiyonlarini kullanabilir.
- Agent baska ekibin ticket'ini veya baska actor'u kendisine/baskasina atayamaz.
- UI'daki `Bana ata` butonu backend policy ile uyumlu hale gelir.
