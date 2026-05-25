# ADR-0003: Servis Sahipli PostgreSQL Schema Izolasyonu

## Karar

Tek PostgreSQL instance kullanilacak, her servis kendi schema'sina ve DB user'ina
sahip olacak.

## Neden

Ortak schema servisleri birbirinin tablolarina baglar. Servis basina ayri DB ise
bu proje icin operasyonel maliyeti gereksiz artirir. Ayri schema, veri sahipligi
ve least privilege icin dengeli yoldur.

## Sonuc

Her servis sadece kendi verisine dogrudan erisir. Servisler arasi veri paylasimi
REST API veya Kafka eventleriyle olur. Bu karar Broken Access Control ve SQL
Injection etkisini sinirlamaya da yardim eder.

