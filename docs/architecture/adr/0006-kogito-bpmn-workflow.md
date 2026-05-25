# ADR-0006: Apache KIE/Kogito BPMN Workflow

## Karar

Ticket lifecycle ve SLA workflow ihtiyaci Apache KIE/Kogito tabanli BPMN sureci
ile karsilanacak.

## Neden

Dokuman jBPM/BPMN beklentisi iceriyor. KIE/Kogito, bu ekosistemin modern
yaklasimidir ve BPMN modelini uygulama mimarisinde gorunur hale getirir.

## Sonuc

Status gecisleri kod icinde rastgele enum mutasyonu olarak kalmaz; workflow
adapter arkasindan ilerler. Entegrasyon basit status yonetimine gore daha
karmasiktir.

