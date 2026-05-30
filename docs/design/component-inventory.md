# Component Inventory

Bu dokuman `FrontendTasarim` referans ekranlarindan cikarilan ilk component
envanteridir. Tech stack secimi sonrasi component isimleri degisebilir, fakat
sorumluluklar korunmalidir.

## Layout Components

| Component | Sorumluluk |
| --- | --- |
| `AppShell` | Sidebar, header ve main workspace iskeleti |
| `SidebarNav` | Role-aware icon navigation, active indicator, tooltips |
| `TopHeader` | Breadcrumb/title, search, notification, help, profile |
| `Workspace` | Sayfa icerik bolgesinin margin ve responsive davranisi |
| `SplitWorkbench` | Sol liste + orta detay + opsiyonel sag panel layout'u |
| `RightDetailPanel` | Customer profile, SLA, history ve aksiyon paneli |

## Ticket Components

| Component | Sorumluluk |
| --- | --- |
| `TicketTable` | Customer ticket listesi, pagination, status chips |
| `TicketQueue` | Agent ticket listesi, search, priority filters |
| `TicketQueueItem` | Ticket id, subject, preview, time, priority |
| `TicketHeader` | Ticket id, title, priority, action buttons |
| `TicketStatusChip` | Acik, beklemede, cozuldu, kapali gibi statuslar |
| `PriorityChip` | Dusuk, normal, yuksek/urgent priority gosterimi |
| `TicketTimeline` | Message, system event ve attachment timeline |
| `TicketComposer` | Reply, attachment, formatting controls, send action |
| `AttachmentItem` | Dosya adi, boyut, type icon, download action |
| `CreateTicketForm` | Konu, kategori, oncelik, aciklama, attachment input |

## Reporting Components

| Component | Sorumluluk |
| --- | --- |
| `KpiStrip` | Toplam ticket, SLA uyumu, ortalama cozum gibi KPI'lar |
| `KpiCard` | Tek KPI, mini trend ve delta |
| `TrendLineChart` | Son 30 gun volume trend |
| `CategoryDistribution` | Kategori bazli oran barlari |
| `AgentPerformanceTable` | Agent bazli cozulmus ticket, ortalama sure, SLA |
| `ReportFilterBar` | Tarih araligi ve rapor filtreleri |

## Shared Form Components

| Component | Sorumluluk |
| --- | --- |
| `Button` | Primary, secondary, ghost varyantlari |
| `IconButton` | Tooltip ve aria-label zorunlu icon aksiyonlari |
| `TextField` | Underline-only input |
| `TextArea` | Underline-only multiline input |
| `SelectField` | Kategori ve filtre secimleri |
| `SegmentedControl` | Priority ve tab filtreleri |
| `FileDropzone` | Dosya secme, validation state, upload progress |
| `Tabs` | Tumu, acik, kapali gibi liste filtreleri |

## State Components

| Component | Sorumluluk |
| --- | --- |
| `LoadingState` | Skeleton veya compact loading satirlari |
| `EmptyState` | Liste bos durumlari |
| `ErrorState` | Guvenli hata mesaji ve retry |
| `ForbiddenState` | Yetkisiz route gorunumu, detay sizdirmadan |
| `OfflineBanner` | API erisim problemi veya network hatasi |

## Implementation Rules

- Componentler shadow veya gradient kullanmaz.
- Layout boyutlari hover veya dynamic content ile kaymamalidir.
- Button ve chip icindeki text uzunluklari responsive davranista tasma
  yapmamalidir.
- Attachment ve message body alanlarinda kullanici icerigi escape edilmelidir.
- Role bazli gizleme component seviyesinde desteklenir fakat guvenlik backend
  authorization ile saglanir.
