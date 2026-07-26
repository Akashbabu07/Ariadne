# Ariadne — Database Design

Owner: Akashbabu07

Last updated: 2026-07-25

Depends on: `overview.md`, `microservice.md`, `event-driven.md`, `security.md`

## 1. Purpose of This Document

This is where architecture becomes concrete enough to start writing entities — but the concreteness
is exactly why it needs to be grounded in the decisions already made, not designed fresh. Every
table here traces back to a data-ownership decision from `microservice.md`; every
org-scoping column traces back to `security.md` Section 4.2. If a table in this doc
doesn't map back to something already decided, that's a signal the earlier docs missed something —
worth resolving there, not patching here.

## 2. Database-per-Service Boundary

**Rule:** each backend service owns its own PostgreSQL schema. No service queries another service's
tables directly — cross-service data access goes through that service's API (sync) or through
consumed Kafka events that populate the consumer's own local read model (async).

**Practical implementation for v1:** one PostgreSQL *instance*, separate *schemas* per service
(`auth`, `project`, `ingestion`, `knowledge`), rather than fully separate database instances. This
gives you the ownership discipline (schema boundaries, no cross-schema joins in application code)
without the operational overhead of managing N separate database instances at portfolio-project
scale. Document this as a deliberate v1 simplification — splitting to separate instances later, if
you ever needed independent scaling or stricter isolation per service, is a migration, not a
redesign, because the ownership boundary was already respected in the schema design.

**Common mistake this prevents:** a "just this once" cross-schema join because two tables happen to
be in the same physical database and it's *possible*. The schema boundary needs to be enforced by
convention/code review discipline now (each service's JPA config only ever points at its own schema)
since the database itself won't stop you the way separate instances would.

## 3. PostgreSQL Schemas by Service

### 3.1 `auth` schema (auth-service)
```
users
  id (uuid, pk)
  org_id (uuid, fk -> organizations.id)
  email (unique within org)
  password_hash
  status (ACTIVE, INVITED, DISABLED)
  created_at, updated_at

roles
  id (uuid, pk)
  name (ADMIN, LEAD, ENGINEER)

permissions
  id (uuid, pk)
  name (e.g. "project:read", "repository:ingest")

role_permissions (join table)
user_roles (join table: user_id, role_id, org_id — role assignment is per-org, since a user could
            theoretically hold different roles in different orgs if Ariadne ever supports that)

refresh_tokens
  id (uuid, pk)
  user_id (fk)
  token_hash (never store raw token)
  expires_at
  revoked_at (nullable)
```
Note `organizations` is *referenced* here but not owned here — see 3.2. This is a legitimate
cross-schema foreign key reference for referential integrity at the DB level even though services
don't query across schemas at the application level; the FK exists for data integrity, application
code still respects the ownership boundary.

### 3.2 `project` schema (project-service)
```
organizations
  id (uuid, pk)
  name
  created_at

projects
  id (uuid, pk)
  org_id (fk)
  name
  created_at

repositories
  id (uuid, pk)
  project_id (fk)
  vcs_provider (GITHUB — enum, ready for GITLAB per the VcsProvider abstraction from
                microservice.md Section 3.5)
  external_repo_id (the provider's own ID/URL)
  name
  default_branch
  last_ingested_at (nullable)
  status (ACTIVE, ARCHIVED)

project_members
  project_id (fk), user_id (references auth.users.id — cross-schema FK, same note as above)
  added_at
```

### 3.3 `ingestion` schema (ingestion-service)
```
ingestion_runs
  id (uuid, pk)
  repository_id (fk -> project.repositories.id)
  triggered_by_user_id (nullable — null if scheduler-triggered, per
                         security.md Section 4.2's SYSTEM sentinel concept)
  commit_sha
  status (PENDING, IN_PROGRESS, COMPLETED, FAILED)
  started_at, completed_at
  error_message (nullable)
```
Deliberately minimal — ingestion-service's real "output" is the Kafka event and the cloned content
in object storage, not a rich relational model. This table exists for status tracking/audit, not as
the source of truth for repository content.

### 3.4 `knowledge` schema (knowledge-service)
```
knowledge_entities
  id (uuid, pk)
  repository_id (fk)
  org_id (denormalized — present for query scoping even though repository_id could look it up via
          project-service, per the "tag the data itself" principle from
          security.md Section 4.2)
  entity_type (SERVICE, CLASS, FUNCTION, API_ENDPOINT, ...)
  qualified_name
  file_path
  neo4j_node_id (reference to the corresponding graph node — see Section 4)
  extracted_at

knowledge_entity_versions
  entity_id (fk)
  commit_sha
  content_hash
  captured_at
```
The `_versions` table exists because knowledge-service needs to answer "what did this entity look
like at commit X" for impact analysis over time — not just current state. Worth deciding now whether
you keep full history from v1 or add it later; recommend starting with it since retrofitting
historical tracking onto a system already in use is painful, consistent with the pattern in every
doc so far of "cheap now, expensive to retrofit."

## 4. Neo4j Graph Schema

**Node labels:**
```
(:Repository {id, name, orgId})
(:Service {id, name})           // a logical service within a repo, e.g. detected from folder structure
(:Class {id, qualifiedName})
(:Function {id, qualifiedName})
(:ApiEndpoint {id, path, method})
```

**Relationship types:**
```
(:Repository)-[:CONTAINS]->(:Service)
(:Service)-[:CONTAINS]->(:Class)
(:Class)-[:CONTAINS]->(:Function)
(:Function)-[:CALLS]->(:Function)
(:Service)-[:DEPENDS_ON]->(:Service)      // derived/aggregated from lower-level CALLS relationships
(:ApiEndpoint)-[:HANDLED_BY]->(:Function)
```

**Why `DEPENDS_ON` is derived, not directly extracted:** service-level dependency is an aggregation
over many function-level `CALLS` relationships, computed by analysis-service (per
`microservice.md` Section 3.9's role as the interpretation layer) rather than
written directly by knowledge-service. This keeps knowledge-service's job purely "record what
parsing found" and analysis-service's job "interpret what it means" — the same separation of
concerns established when graph-service and analysis-service were split in the earlier doc, now
showing up concretely in what writes which relationship type.

**Org scoping in Neo4j:** every node carries `orgId` (per `security.md` Section 4.2)
— all graph-service queries must filter by `orgId`, enforced in graph-service's query layer, not
left to callers to remember. Worth a shared query-builder helper in graph-service specifically so
this can't be forgotten on a new endpoint.

## 5. pgvector Schema (embedding-service)

```
code_chunks
  id (uuid, pk)
  repository_id
  org_id (same scoping principle again)
  knowledge_entity_id (fk -> knowledge.knowledge_entities.id, links a chunk back to the structural
                        entity it came from — this is what lets rag-service's hybrid retrieval,
                        per 04-ai-architecture.md Section 4, jump from a vector hit to graph context)
  file_path
  chunk_text
  embedding (vector(384))   -- dimension depends on chosen Sentence Transformers model, confirm
                             -- and lock this in before any real ingestion, since changing embedding
                             -- dimension later means re-embedding everything
  chunk_index
  created_at
```

An HNSW or IVFFlat index on `embedding` — pick based on expected corpus size (HNSW generally
preferred for the dataset sizes Ariadne will likely operate at; document the actual choice once
you've benchmarked, don't just default without checking).

**The `knowledge_entity_id` foreign key is the single most important design decision in this
section** — it's the concrete implementation of the "hybrid retrieval" idea from
`ai-architecture.md`. Without this link, vector search and graph traversal are two disconnected
systems that happen to both return results about the same repo; with it, rag-service can go
vector-hit → entity → graph neighborhood in one join, which is the actual mechanism behind Section
4's context assembly flow.

## 6. Migration Strategy

Flyway, per-service — each service's schema migrations live in that service's own module
(`src/main/resources/db/migration`), versioned independently. A service should never depend on
another service's migrations running first (reinforces the ownership boundary from Section 2) —
if `knowledge` schema needs to reference `project.repositories.id`, that FK is fine (Section 3.1's
note on cross-schema FK for integrity), but `knowledge`'s migrations should not be responsible for
creating or altering `project`'s tables.

---
## 8. Next Document

`docs/architecture/api-design.md` — REST API conventions across services (resource naming,
pagination, error response shape building on the `shared` response envelope), OpenAPI/Swagger
standards, and the api-gateway's routing table.