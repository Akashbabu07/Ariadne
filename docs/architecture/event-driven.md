# Ariadne — Event-Driven Architecture


Owner: AKashbabu07

Last updated: 2026-07-25

Depends on: `overview.md`, `microservice.md`

## 1. Purpose of This Document

Section 5/6 of `microservice.md` established *that* ingestion → parsing → knowledge
extraction happens asynchronously via Kafka. This document defines *how*: topic naming, event schema
shape and versioning, ordering guarantees, idempotency, failure handling, and — critically — where
the synchronous/asynchronous line actually gets drawn when a real request comes in, not just in the
abstract.

Get this document wrong and you don't find out until production: duplicate events double-processing
a repo, an event schema change silently breaking a consumer that deserializes the old shape, or a
retry storm during a GitHub outage cascading into every downstream service. This is the highest-risk
document in the set so far — worth being deliberate here.

## 2. Why Event-Driven at All (and where it stops)

Not everything should be a Kafka event. The rule from the previous doc: **asynchronous where the
producer doesn't need to wait for the consumer to proceed, synchronous where it does.**

Concretely in Ariadne:
- Ingestion → Parsing → Knowledge extraction → Embedding: a **pipeline**. Each stage's output
  triggers the next. Nobody's HTTP request is blocked waiting for the whole pipeline — the user who
  clicked "ingest this repo" gets an immediate "ingestion started" response, not a response that
  blocks until parsing finishes.
- A user asking the AI Assistant a question via `rag-service`: **synchronous**, because the user is
  sitting there waiting for an answer. This does not belong on Kafka.
- Analysis-service querying graph-service for a traversal to build a dependency report: **synchronous
  REST/Feign call**, because analysis-service needs the graph result *right now* to keep computing.

**Common mistake:** making everything async "because event-driven is the architecture style." Event
-driven is a tool for decoupling pipeline stages with no immediate response requirement — not a
blanket rule. Half of Ariadne's actual traffic (search queries, AI chat, dashboard reads) should be
plain synchronous REST and that's correct, not a compromise.

## 3. Topic Naming Convention

Format: `<domain>.<entity>.<event-past-tense>`

| Topic | Producer | Consumer(s) | Purpose |
|---|---|---|---|
| `ingestion.repository.ingested` | ingestion-service | parser-service | Repo cloned & metadata extracted, ready for parsing |
| `ingestion.repository.ingestion-failed` | ingestion-service | project-service, integration-service | Ingestion failed — surface to user, notify |
| `parsing.repository.parsed` | parser-service | knowledge-service, embedding-service | AST/structural extraction complete |
| `parsing.repository.parse-failed` | parser-service | knowledge-service, integration-service | Parsing failed for this repo (or specific files) |
| `knowledge.graph.updated` | knowledge-service | graph-service, analysis-service | Graph has new/changed entities — downstream caches may need invalidating |
| `embedding.chunks.embedded` | embedding-service | search-service | New embeddings available for search indexing |
| `analysis.drift.detected` | analysis-service | integration-service (notifications) | Architecture drift found — notify subscribers |

**Why past-tense, domain-prefixed:** past tense signals "this already happened" (an immutable fact,
which is what Kafka events should represent) rather than a command ("do this"). Domain-prefixing
avoids collisions as the topic list grows and makes ownership obvious from the name alone — you can
tell which service owns a topic without opening a wiki.

**Common mistake:** naming topics after the technical action instead of the domain event —
`kafka-repo-topic-1` tells you nothing six months from now. `ingestion.repository.ingested` still
will.

## 4. Event Schema Shape

Every event on every topic shares a common envelope, defined once in `shared` (per
`02-microservice-architecture.md` Section 3.1 — this is exactly the kind of contract `shared` should
hold):

```json
{
  "eventId": "uuid",
  "eventType": "ingestion.repository.ingested",
  "eventVersion": 1,
  "occurredAt": "2026-07-25T10:15:30Z",
  "producedBy": "ingestion-service",
  "correlationId": "uuid",
  "payload": {
    "repositoryId": "uuid",
    "projectId": "uuid",
    "commitSha": "abc123",
    "cloneUri": "s3://..."
  }
}
```

- **`eventId`**: unique per event instance — this is what idempotency checks key off (Section 6).
- **`correlationId`**: shared across every event in one pipeline run (e.g. one ingestion → parse →
  embed chain), so you can trace one repository's journey through the whole system in your
  observability tooling (this is where OpenTelemetry/Zipkin from the tech stack actually earns its
  place — trace a `correlationId` end to end).
- **`eventVersion`**: see Section 5.
- **`payload`**: entity-specific, minimal — IDs and references, not full denormalized objects. If a
  consumer needs more detail than the ID, it calls the owning service's REST API or looks it up from
  its own consumed state. This keeps events small and avoids every payload change requiring every
  consumer to update.

## 5. Schema Versioning

Add `eventVersion` to every event from day one, even at v1 — retrofitting this later, once
consumers exist that assume an unversioned shape, is exactly the "expensive to retrofit" situation
we're avoiding elsewhere in this design.

**Rules:**
- **Additive changes** (new optional field) do not bump `eventVersion` — consumers must ignore
  unknown fields rather than fail deserialization (this is a consumer-side implementation
  requirement, not optional).
- **Breaking changes** (removed field, renamed field, changed semantics of an existing field) bump
  `eventVersion` and the producer should, for a transition period, either dual-publish both versions
  or the consumer must handle both versions explicitly. Do not silently break old consumers.
- Schema definitions for every event type live in `shared`, as Java records/DTOs, so the compiler
  catches producer/consumer schema drift at build time within the monorepo — one of the real
  practical advantages of monorepo you flagged earlier translating directly into safety here.

## 6. Ordering and Idempotency

**Ordering:** Kafka guarantees order only within a partition. Partition key = `repositoryId` for all
ingestion/parsing/knowledge topics — this guarantees all events for one repository are processed in
order relative to each other, while still allowing full parallelism across different repositories
(which is the actual scaling need — many repos processed concurrently, each internally ordered).

**Idempotency — this matters more than ordering in practice.** Consumers *will* see duplicate
events — from producer retries, consumer group rebalances, or at-least-once delivery semantics,
which is what we want here (Kafka can guarantee exactly-once within limited scope, but across
service boundaries with external side effects, at-least-once + idempotent consumers is the more
robust and standard pattern). Every consumer must be safe to process the same event twice:

- Knowledge-service: upsert by entity ID, not insert — reprocessing the same `parsed` event should
  produce the same graph state, not duplicate nodes.
- Track processed `eventId`s (e.g. a small dedup table or Redis set with TTL) for consumers where
  upsert-by-ID alone isn't sufficient (e.g. triggering a side effect like a notification, which
  isn't naturally idempotent the way a database upsert is).

**Common mistake:** assuming Kafka's at-least-once delivery is a corner case you'll rarely hit. In
practice, consumer group rebalances (a pod restarting during a deploy, for instance) cause duplicate
delivery routinely, not rarely. Idempotent consumers are not optional hardening — they're required
for correctness from the first version of each consumer.

## 7. Failure Handling

- **Retry with backoff** at the consumer level for transient failures (e.g. knowledge-service's
  Neo4j connection blips).
- **Dead-letter topic** per main topic (e.g. `parsing.repository.parsed.dlt`) for events that fail
  repeatedly — don't let one poison-pill event block the partition indefinitely for every other
  repository queued behind it.
- **Failure events are still events**: note `ingestion.repository.ingestion-failed` and
  `parsing.repository.parse-failed` in the topic table above — failures propagate through the same
  event-driven mechanism so integration-service can notify the user, rather than failures being
  silent or requiring polling.

## 8. What This Means for `shared`

Concretely, `shared` needs, before any service starts producing/consuming real events:
- `BaseEvent` envelope record (Section 4)
- One record per event payload type (`RepositoryIngestedPayload`, `RepositoryParsedPayload`, ...)
- Kafka topic name constants (avoid magic strings scattered across producers/consumers)
- A shared idempotency-check utility/interface, even if each service's storage backend for "seen
  event IDs" differs
---
## 10. Next Document

`docs/architecture/ai-architecture.md` — how ai-gateway coordinates parser-service,
embedding-service, rag-service, and reasoning-service; where RAG context gets built from the
knowledge graph + vector search combined; and the reasoning-service's LangGraph workflow shape for
impact analysis and drift detection.