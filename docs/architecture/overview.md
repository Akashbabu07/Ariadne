# Architecture Overview

## Status
Draft — services are scaffolded, no business logic implemented yet.

## System Summary
ATLAS is a microservices-based AI engineering intelligence platform. Backend is Java 21 / Spring Boot 4.0.7, built as a Maven multi-module project.

## Services

| Service | Responsibility | Status |
|---|---|---|
| api-gateway | Single entry point, routing, cross-cutting concerns | Scaffold only |
| auth-service | Authentication & authorization (JWT via jjwt) | Scaffold only |
| project-service | Project/workspace management | Scaffold only |
| ingestion-service | Data ingestion pipeline | Scaffold only |
| knowledge-service | Knowledge base storage/retrieval | Scaffold only |
| search-service | Search indexing/querying | Scaffold only |
| analysis-service | Analysis/processing logic | Scaffold only |
| graph-service | Graph data storage/queries | Scaffold only |
| ai-orchestrator | Coordinates AI/LLM calls across services | Scaffold only |
| integration-service | Third-party integrations | Scaffold only |
| scheduler-service | Scheduled/background jobs | Scaffold only |

## Open Questions
- Service discovery mechanism (Eureka / Consul / static config)?
- Sync (REST/OpenFeign) vs async (Kafka/RabbitMQ) communication between services?
- Is a `shared` module needed for common DTOs, error handling, auth utilities? (currently commented out in root `pom.xml`)
- Frontend stack and repo location — not present in this archive yet.

## Related Docs
- See `adr/` for decisions as they're made.
- See `diagrams/` for visual system diagrams.
