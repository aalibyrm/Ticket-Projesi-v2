# Mobile Screen Inventory

Bu dokuman `MobilTasarim` klasorundeki mobil UI referanslarini resmi
gelistirme kaynagi olarak ozetler.

## Tasarim Sistemi

Kaynak: `MobilTasarim/precision_minimalist/DESIGN.md`

- Duz yuzey, golge ve gradient yok.
- Kart, input, sheet ve butonlarda 1px `#E5E5EA` border.
- Ana arka plan `#FCFCFC`, container yuzeyi `#FFFFFF`.
- Primary kirmizi `#AA1101` sadece kritik aksiyon, SLA riski ve aktif vurgu
  icin kullanilir.
- Mobil spacing 4px grid uzerinden ilerler; ana yatay padding 16px-24px.
- Primary container radius 8px.
- Inputlar underline-only stilde uygulanir.

## Customer Ekranlari

| Kaynak | Uygulama karsiligi | Not |
| --- | --- | --- |
| `new_ticket_creation` | Yeni ticket formu | Konu, kategori/topic, oncelik, aciklama ve dosya ekleme |
| `ticket_history` | Musteri ticket listesi | Tumu/acik/beklemede/kapali filtreleri |
| `message_list_revised_precision_minimalist` | Mesaj listesi | Ticket arama, unread indicator ve bottom tab |
| `customer_active_conversation_bottom_sheet_open` | Ticket mesaj detayi + bilgi sheet | Agent bilgisi, SLA durumu, ticket bilgileri ve kapatma aksiyonu |
| `active_conversation_closed_customer_view` | Kapali ticket gorunumu | `screen.png` bozuk kaynak dosya olarak geldi; uygulanirken tekrar dogrulanmali |

## Agent Ekranlari

| Kaynak | Uygulama karsiligi | Not |
| --- | --- | --- |
| `agent_ticket_queue` | Agent kuyruk | Atanmamis ticket ustlenme, arama ve filtre |
| `agent_landing_assigned_tickets` | Atanan ticket listesi | SLA kalan sure ve oncelik filtreleri |
| `agent_active_chat_no_sheet` | Agent mesaj detayi | Yanıt inputu, dosya eki ve rich text kisa yollar |
| `agent_customer_context_bottom_sheet` | Musteri context sheet | Musteri gecmisi, istatistikler, kapat/escalate aksiyonlari |

## Manager Ekranlari

| Kaynak | Uygulama karsiligi | Not |
| --- | --- | --- |
| `manager_dashboard_editorial_statistics` | Mobil manager ozet | KPI, SLA uyumu, son 30 gun, kategori dagilimi ve ekip performansi |

## Implementasyon Notlari

- #54 sadece Expo Managed scaffold, auth, navigation, API client ve env config
  kurar.
- #55 bu ekrani gercek React Native componentlerine tasir.
- #55 icin kullanici A secenegini secti: ekranlar Pure React Native
  `StyleSheet` ve shared design token yaklasimiyla uygulanir.
- Customer, agent ve manager navigation ayni app icinde role gore ayrilir.
- Dosya ekleme islemi mobilde Expo DocumentPicker ile dosya secer ve
  file-service presigned URL modelini kullanir; ticket authorization karari
  backend tarafinda kalir.
