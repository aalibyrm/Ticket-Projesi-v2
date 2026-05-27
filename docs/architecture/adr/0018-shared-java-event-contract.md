# ADR-0018: Shared Java Event Contract

## Karar

Kafka event sozlesmeleri icin `libs/event-contract` adinda ortak bir Java Maven
module'u kullanilacak. Bu module event envelope, topic adlari, event type'lari,
version policy ve minimal payload policy kurallarini paylasir.

## Neden

Bu projede Java disinda programlama dili kullanilmayacak. Producer ve consumer
servislerin event sozlesmesini compile-time seviyesinde paylasmasi, topic adi,
event type ve envelope alanlarindaki hatalari erken yakalar.

Kod yazma maliyeti sorun olmasa da AsyncAPI/JSON Schema yaklasimi bu asamada
ek surec maliyeti getirir: schema drift kontrolu, generator karari,
compatibility pipeline ve iki kaynakli contract bakimi gerekir. Henuz dis
consumer veya Java disi servis olmadigi icin bu maliyet #21 icin fazla kabul
edildi.

## Alternatifler

- Sadece dokumantasyon: En hizli yoldur, ancak servisler compile-time guard
  alamaz ve event sozlesmesi kolayca dagilir.
- AsyncAPI/JSON Schema: Buyuk organizasyonlarda guclu contract-first
  yaklasimdir. Ancak Java-only hedefi, dis consumer olmamasi ve overengineering
  istemememiz nedeniyle simdilik ertelendi.
- Schema Registry: Kafka ekosisteminde gucludur, fakat bu fazda Avro/Protobuf
  format karari ve ekstra altyapi gerektirir.

## Sonuc

Event contract mikroservisler arasinda ortak library olarak paylasilir, fakat bu
module icine domain logic veya servis implementasyonu konulmaz. Bu sinir
korundugu surece shared module mikroservis mimarisini monolitik hale getirmez.

AsyncAPI/JSON Schema ileride farkli ekipler, dis sistem entegrasyonu veya Java
disi consumer gereksinimi dogarsa ikinci asama olarak degerlendirilecektir.
