# ADR-0039: Agent Portal Ticket Authority

## Karar

#52 kapsaminda agent web portal akislari REST uzerinden `ticket-service` ve
`file-service` ile calisir:

- Agent ticket queue, ticket detail, status transition, assignment, external
  reply, internal note ve worklog akislari `ticket-service` endpointleri
  uzerinden calisir.
- Agent detail attachment metadata okuma ve download URL uretimi mevcut
  `file-service` presigned URL akisiyle korunur.
- Frontend server-state cache ve invalidation islemleri TanStack Query ile
  yonetilir; Redux sadece auth/client state icin kullanilir.

Ticket-service agent endpointleri sadece support actor context ile calisir.
Actor context JWT subject ve realm role ile baslar; team membership ve team
lead kapsami #63/ADR-0043 sonrasinda ticket-service DB'sindeki aktif
`team_members` kayitlarindan cozulur. Local test/dev header'lari sadece JWT
olmayan lokal profiller icindir ve team scope yetkisi vermez.

## Neden

Agent portal tek ekranda queue, conversation, action panel, worklog ve dosya
bilgisini birlestirir; ancak ticket sahipligi ve yetki karari ticket-service
tarafinda kalmalidir. Bu nedenle frontend assignment veya status buttonlarini
gizlese bile backend dogrudan URL erisiminde tekrar yetki kontrolu yapar.

Agent icin okuma yetkisi `assigned agent`, aktif `assigned team member`,
`manager` veya `admin` olarak sinirlandi. Yonetim yetkisi assigned agent, aktif
team lead veya admin ile sinirlidir. Customer endpointleriyle agent endpointleri
ayrildi; internal note endpointi customer-visible external comment endpointinden
ayri tutuldu.

## Sonuc

Agent yalnizca kendisine veya aktif ekiplerine atanmis ticket'lari queue/detail
icinde gorur; assigned agent ve team lead kapsaminda yonetir. Manager tum
ticket'lari read-only gorur, admin tum ticket'lari yonetebilir. Internal note
customer yorum listesine dahil edilmez ve notification event'i uretmez.
Attachment gorme/indirme file-service presigned URL akisini kullanmaya devam
eder, bu sayede dosya domain'i ticket-service icine gomulmez.

Bu karar mikroservis sinirlarini korurken agent operasyon ekranini #52 icin
tamamlar. Daha sonra user/team directory veya unassigned queue eklenecekse yeni
issue ve gerekirse yeni ADR ile kapsam genisletilmelidir.
