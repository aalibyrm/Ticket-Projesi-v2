# ADR-0067: Topic Round-Robin Team Routing

## Status

Accepted

## Context

Finance Operations altinda Payment Operations 1 ve Payment Operations 2
ekipleri olusturulduktan sonra, ayni ihtiyacin sadece payment icin degil,
herhangi bir departman/topic altinda birden fazla takim oldugunda da
gecerli olabilecegi netlesti. Customer ticket acarken takim secmemeli; routing
karari backend tarafinda, DB kaynakli ve denetlenebilir olmalidir.

Agent assignment ekranindaki ekip secimi de kafa karistiriyordu. Lead'in baska
takima ticket tasimasi ayri bir operasyon oldugu icin normal assignment UI'inda
yer almamalidir.

## Decision

Kullanici #98 kapsaminda su karari verdi:

- Ticket acilisinda customer sadece topic secer; takim secmez.
- Bir topic icin tek aktif routing rule varsa mevcut davranis korunur.
- Bir topic icin birden fazla aktif takim route'u varsa ticket-service bu
  takimlar arasinda topic bazli round-robin uygular.
- Round-robin sirasi `ticket_routing_rules.routing_order` ile belirlenir.
- Round-robin state `ticket_routing_cursors` tablosunda tutulur ve ticket
  create transaction'i icinde lock'lanir.
- Agent assignment UI'indan ekip secimi kaldirilir.
- Lead yalniz ticket'in mevcut routed team'i icindeki agent'i secebilir.
- Lead olmayan agent yalniz kendi adina self-assignment yapabilir.
- Cross-team ticket transfer bu kapsamda yoktur; gerekirse ileride admin-only
  ayri endpoint ve karar ile ele alinir.

## Consequences

- Payment Operations 1 ve Payment Operations 2 yeni payment ticket'larini sirali
  olarak alir; ayni model ileride baska topic'lere ek takim tanimlaninca da
  calisir.
- Customer veya frontend raw `assignedTeamId` gondererek routing'i manipule
  edemez; OWASP Broken Access Control riski azalir.
- Routing davranisi deployment gerektirmeden DB route kayitlariyla genisletilir,
  fakat route sirasi ve cursor state'i operasyonel veri haline gelir.
- Workload bazli kapasite optimizasyonu yapilmaz; bu karar bilincli olarak daha
  sade ve deterministik round-robin modelini secer.
