# Ariadne — API Design

Owner: AkashBabu07
Last updated: 2026-07-25
Depends on: `microservice.md`, `event-driven.md`, `security.md`

## 1. Purpose of This Document

Eleven backend services plus four Python services means eleven-plus independently evolving API
surfaces unless conventions are fixed centrally, once, here. Inconsistent pagination, inconsistent
error shapes, or inconsistent resource naming across services is one of the fastest ways a
microservice project starts *feeling* like a pile of separately-built things rather than one
platform — this doc exists to prevent that, the same way `shared`'s event envelope prevented it for
Kafka events in `event-driven.md`.

## 2. Resource Naming and URL Structure

```
/api/v1/{resource}                  GET (list), POST (create)
/api/v1/{resource}/{id}             GET, PATCH, DELETE
/api/v1/{resource}/{id}/{sub-resource}   for genuine sub-resources (owned, not just related)
```

- Plural nouns (`/projects`, not `/project`), lowercase, hyphenated for multi-word resources
  (`/knowledge-entities`, not `/knowledgeEntities`).
- Reserve nested URLs for true ownership (`/projects/{id}/repositories` — a repository doesn't
  exist independent of its project). Use query parameters for filtering by relationship otherwise
  (`/knowledge-entities?repositoryId=...`, not `/repositories/{id}/knowledge-entities`) — knowledge
  entities are owned by knowledge-service per `02-microservice-architecture.md`, not by
  project-service, and the URL structure should reflect actual ownership boundaries, not just
  "this is related to that."
- Actions that don't map cleanly to CRUD get an explicit verb sub-path rather than being forced into
  a fake resource: `POST /repositories/{id}/ingest` (triggers ingestion — a command, correctly
  modeled as a POST to an action, not a fake "ingestion resource" being created).

## 3. Versioning

`/api/v1/` prefix from day one, even though there's only one version right now — retrofitting a
version prefix into URLs already in use by a frontend (and, eventually, external API consumers if
Ariadne ever exposes a public API per the "real SaaS product" ambition) is the kind of change that's
trivial now and painful later, same pattern as every "cheap now, expensive to retrofit" note in the
earlier docs.

**Breaking vs. non-breaking**, mirroring the event schema rule in
`event-driven.md` Section 5: additive fields in responses are non-breaking; removed
or renamed fields, or changed semantics, require a new version or, at minimum, an explicit
deprecation window communicated via the OpenAPI spec (Section 6).

## 4. Standard Response Envelope

Lives in `shared`, alongside the Kafka event envelope — same instinct, same reasoning: one place
defines the contract, every service's controllers use it, no drift.

**Success:**
```json
{
  "success": true,
  "data": { },
  "meta": {
    "correlationId": "uuid",
    "timestamp": "2026-07-25T10:15:30Z"
  }
}
```

**Error:**
```json
{
  "success": false,
  "error": {
    "code": "REPOSITORY_NOT_FOUND",
    "message": "Repository with id ... was not found",
    "details": []
  },
  "meta": {
    "correlationId": "uuid",
    "timestamp": "2026-07-25T10:15:30Z"
  }
}
```

**Why `correlationId` in every response, not just events:** this is the same trace ID concept from
`event-driven.md` Section 4, extended to the synchronous side. A single user action
(e.g. "ingest this repo") starts as an HTTP request, kicks off an async pipeline, and eventually
surfaces a notification — one `correlationId` threading through the HTTP response, the Kafka events,
and (per `security.md` Section 4.2) the event's `initiatedByUserId`, means the whole
journey is traceable in your observability tooling as one unit, not three disconnected logs.

**Error codes, not just messages:** `code` is a stable machine-readable string (`REPOSITORY_NOT_FOUND`),
`message` is human-readable and can change wording without breaking frontend logic that branches on
error type. Define the error code enum in `shared` per service domain, so codes are namespaced and
don't collide (`REPOSITORY_NOT_FOUND` vs a hypothetical unrelated `NOT_FOUND` from another service).

## 5. Pagination

Cursor-based, not offset-based, for any endpoint whose result set can grow large (knowledge
entities, search results) — offset pagination degrades in performance and correctness (items
shifting between pages as data changes) exactly on the endpoints where Ariadne's data volume will
actually be large. Offset pagination is acceptable only for genuinely small, bounded lists
(e.g. `/projects` for a given org — unlikely to be thousands of rows).

```json
{
  "data": [ ],
  "meta": {
    "nextCursor": "opaque-string-or-null",
    "hasMore": true
  }
}
```

## 6. OpenAPI/Swagger Standards

- Every service generates its own OpenAPI spec (springdoc-openapi, matches your tech stack's
  Swagger/OpenAPI entry) — served at `/v3/api-docs` per service, aggregated at api-gateway for a
  unified developer-facing view.
- DTOs used in API contracts should be explicit request/response DTOs, not domain entities directly
  — this is standard Clean Architecture practice per your own stated principles, and it matters
  concretely here because it means a database schema change (Section 6 of
  `database-design.md`) doesn't automatically become an API breaking change; the DTO is the
  seam that absorbs it.
- Every endpoint documented with example request/response bodies — not just types — since this spec
  is also effectively the contract the frontend team (future you, or an actual team later) builds
  against.

## 7. API Gateway Routing Table (initial)

| Path prefix | Routes to |
|---|---|
| `/api/v1/auth/**` | auth-service |
| `/api/v1/projects/**`, `/api/v1/repositories/**` | project-service |
| `/api/v1/ingestion/**` | ingestion-service |
| `/api/v1/knowledge/**` | knowledge-service |
| `/api/v1/search/**` | search-service |
| `/api/v1/analysis/**` | analysis-service |
| `/api/v1/graph/**` | graph-service |
| `/api/v1/ai/**` | ai-gateway |
| `/api/v1/integrations/**` | integration-service |

Gateway responsibilities at this layer, concretely (expanding `microservice.md`
Section 3.2): JWT validation, path-based routing per this table, per-route rate limiting (tighter
limits on `/ai/**` given LLM call cost, looser on read-heavy `/search/**`), and translating any
unhandled downstream 5xx into the standard error envelope (Section 4) rather than leaking a raw
Spring Boot error page to the client.

## 8. Idempotency for Mutating Requests

Distinct from Kafka idempotency (`event-driven.md` Section 6) but the same
underlying concern: a client retry (network blip, double-click) shouldn't double-ingest a repo or
double-create a project. For unsafe operations that aren't naturally idempotent (`POST
/repositories/{id}/ingest` being the clearest example — calling it twice shouldn't start two
ingestion runs for the same commit), accept an optional `Idempotency-Key` header; the service checks
for a prior request with the same key within a time window and returns the original result instead
of re-executing.
---

## 10. Next Document

With API conventions fixed, the next document is `docs/architecture/diagrams.md` — consolidating
the Mermaid diagrams from docs 01–07 into one browsable set (system context, service interaction,
event flow, sequence diagrams for the key user journeys), which closes out the pure-documentation
phase before Step 6 in your development philosophy: building the `shared` module.