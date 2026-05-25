# ticket-service

Core ticket domain service. This service owns `ticket_schema`.

## Port

Default port: `8081`

## Database

Expected local datasource:

```properties
TICKET_DB_URL=jdbc:postgresql://localhost:5432/ticket_platform
TICKET_DB_USER=ticket_app
TICKET_DB_PASSWORD=ticket_dev_password
```

The PostgreSQL role must only have access to `ticket_schema`. Cross-service
schema access is intentionally not granted.

## Local Run

```powershell
mvn -pl services/ticket-service spring-boot:run -Dspring-boot.run.profiles=local
```

Health endpoint:

```text
GET http://localhost:8081/actuator/health
```

