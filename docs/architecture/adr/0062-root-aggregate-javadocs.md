# ADR-0062: Root aggregate Javadocs

## Status
Accepted

## Context
The backend is a Maven reactor with multiple Spring Boot microservices and one
shared event contract library. The project needs Javadocs without introducing a
separate documentation service or language outside Java/Maven.

## Decision
Generate aggregate Javadocs from the root Maven reactor with the Apache Maven
Javadoc Plugin.

The canonical command is:

```powershell
mvn -DskipTests javadoc:aggregate
```

The generated documentation is written to `target/reports/apidocs/index.html`.

## Consequences
- Backend Java API documentation is produced by the same Maven toolchain as the
  services.
- The root POM owns Java version, encoding, Java 21 API links, and doclint
  behavior consistently.
- CI can add this command later without inventing a separate documentation
  pipeline.
- Generated Javadocs remain build artifacts and are not committed.
