# ADR-0061: Swagger/OpenAPI aggregation

## Status
Accepted

## Context
The project needs Swagger/OpenAPI documentation without turning the API Gateway
into the owner of every service contract. REST API v1 is exposed publicly as
`/api/v1/**`, while service-local controllers still use `/api/**`.

## Decision
Use Springdoc per microservice and aggregate those specs in the API Gateway
Swagger UI.

Each service generates `/v3/api-docs` for its own public `/api/**` controllers.
The generated spec rewrites documented paths to `/api/v1/**` so the contract
matches the public gateway surface. Internal endpoints such as `/internal/**`
are excluded from OpenAPI generation.

The API Gateway exposes a single Swagger UI at `/swagger-ui.html` and proxies
service specs under `/v3/api-docs/{service-name}`.

## Consequences
- Each microservice remains the owner of its own API contract.
- Frontend and mobile consumers see the gateway-facing `/api/v1/**` API.
- Gateway aggregation avoids copying controller annotations or OpenAPI files
  into a central module.
- Production deployments can disable docs with Springdoc environment flags when
  API documentation should not be publicly exposed.
