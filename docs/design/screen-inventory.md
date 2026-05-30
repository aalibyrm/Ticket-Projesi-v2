# Screen Inventory

Bu dokuman `FrontendTasarim` altindaki ekranlari frontend route ve feature
kapsamina baglar. Statik `code.html` dosyalari ve `screen.png` goruntuleri
tasarim referansidir; production React kodu olarak dogrudan kopyalanmayacaktir.

## Screens

| Kaynak | Rol | Onerilen route | Amac | Sonraki implementation kapsami |
| --- | --- | --- | --- | --- |
| `FrontendTasarim/taleplerim` | Customer | `/tickets` | Musterinin kendi ticket listesini gormesi | Customer ticket list |
| `FrontendTasarim/yeni_destek_talebi` | Customer | `/tickets/new` | Yeni ticket olusturma ve opsiyonel dosya ekleme | Create ticket + attachment upload |
| `FrontendTasarim/temsilci_paneli_revize` | Agent | `/agent/inbox` | Atanan ticket kuyrugu ve hizli cevaplama | Agent inbox shell |
| `FrontendTasarim/musteri_detay_panel_acik` | Agent | `/agent/tickets/:ticketId` | Ticket detay, attachment, customer profile, SLA ve aksiyonlar | Agent ticket detail |
| `FrontendTasarim/mesajlar_odaklanmis` | Agent | `/agent/messages/:ticketId` | Conversation odakli mesajlasma | Message workspace |
| `FrontendTasarim/mesajlar_detay_panel_acik` | Agent | `/agent/messages/:ticketId?panel=details` | Conversation + sag SLA/customer paneli | Message workspace detail panel |
| `FrontendTasarim/yonetici_raporu` | Manager | `/reports` | KPI, SLA, kategori dagilimi ve agent performance | Manager reports |

## Route Groups

### Customer

- `/tickets`
- `/tickets/new`
- `/tickets/:ticketId`
- `/notifications`
- `/profile`

Customer sadece kendi ticketlarini gorur. UI listesi bu kurala gore filtreli
gorunur; gercek yetki backend tarafinda korunur.

### Agent

- `/agent/inbox`
- `/agent/tickets/:ticketId`
- `/agent/messages/:ticketId`
- `/notifications`
- `/profile`

Agent ekranlari assigned ticket ve assigned team mantigina gore sekillenir.
Ticket detail icinde reply, attachment goruntuleme, priority/SLA bilgisi ve
close/escalate aksiyonlari bulunur.

### Manager

- `/reports`
- `/reports/sla`
- `/reports/agents`
- `/agent/inbox` read-focused veya operasyonel gorunum
- `/notifications`
- `/profile`

Manager rapor ekranlari reporting-service API'leriyle beslenecektir.

### Admin

- `/admin/settings`
- `/admin/users`
- `/admin/audit`
- `/reports`
- `/notifications`
- `/profile`

Admin ekranlari referans tasarimlarda yoktur. Ilk uygulamada shell ve navigation
hazirlanir; detay ekranlari ayri issue'larda tasarlanir.

## API Alignment

| UI Ihtiyaci | Backend sahibi |
| --- | --- |
| Ticket list/detail/create | `ticket-service` |
| Ticket comment/reply/worklog/action | `ticket-service` |
| Attachment metadata/upload/download | `file-service`, ticket auth karari icin `ticket-service` |
| SLA state/risk/breach | `workflow-sla-service` |
| Notifications | `notification-service` |
| Status, closed tickets, agent performance, SLA reports | `reporting-service` |

## Known Design Adjustments

- `yonetici_raporu` ekraninda brand/sidebar tasmasi implementation sirasinda
  duzeltilmelidir.
- Agent conversation ekranlari icin tek bir reusable workbench layout
  olusturulmalidir; `mesajlar_odaklanmis` ve `mesajlar_detay_panel_acik` ayni
  route ailesinin panel acik/kapali durumlari olarak ele alinmalidir.
- Mobile responsive davranis referans ekranlarda tanimli degil; web uygulamasi
  desktop-first, tablet-safe olarak baslayacak, mobile web polish sonraki
  issue'larda ele alinacaktir.
