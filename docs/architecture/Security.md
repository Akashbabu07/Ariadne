# Ariadne — Security Architecture

Owner: Akashbabu7

Last updated: 2026-07-25

Depends on: `overview.md`, `microservice.md`, `event-driven.md`

## 1. Purpose of This Document

Ariadne's security surface is larger than a typical CRUD app's for one specific reason worth naming
up front: **it ingests source code**, which is often an organization's most sensitive asset. Auth
design here isn't just "log in and see your data" — it's the difference between a portfolio project
and something a real engineering org could trust with its codebase. This document covers identity
(auth-service), authorization (RBAC), how identity propagates across every boundary established in
the previous three docs (gateway → services, services → Kafka events, Java → Python), and secrets
management for the external credentials Ariadne accumulates (GitHub tokens, Slack, Jira, LLM keys).

## 2. Identity Model

### 2.1 Tokens
- **Access token**: short-lived JWT (recommend 15 min), signed (RS256, not HS256 — asymmetric
  signing means only auth-service holds the private key, but any service can verify with the public
  key without calling auth-service, which is exactly the "validate locally" pattern from
  `microservice.md` Section 3.3).
- **Refresh token**: longer-lived, opaque (not a JWT — no reason to expose claims in something
  that's purely "let me get a new access token"), stored server-side (in auth-service's DB) so it
  can be revoked. Refresh token rotation on use (each refresh issues a new refresh token and
  invalidates the old one) — this limits the damage window if a refresh token leaks, since a reused
  old token becomes a detectable signal of compromise.

### 2.2 JWT Claims
```json
{
  "sub": "user-uuid",
  "orgId": "org-uuid",
  "roles": ["ENGINEER"],
  "permissions": ["project:read", "repository:ingest"],
  "iat": 1234567890,
  "exp": 1234568790
}
```
Include `orgId` directly in the claim — every domain query in every service should be scoped by
org, and having it in the token means services enforce scoping without an extra lookup. This is
also your first line of defense for multi-tenancy if Ariadne ever serves more than one organization
per deployment — worth getting right now rather than retrofitting.

## 3. RBAC Model

From Section 3 of `overview.md`, the actors are roles, not necessarily distinct accounts.
Minimum viable role set for v1:

| Role | Represents | Example permissions |
|---|---|---|
| `ADMIN` | Org owner/admin | manage members, manage all projects, manage integrations |
| `LEAD` | Tech lead/architect | full read on analysis/graph, manage project settings, trigger re-ingestion |
| `ENGINEER` | Individual contributor | read graph/search/analysis, use AI assistant, cannot manage integrations or members |

Permissions are the actual enforcement unit (`project:read`, `repository:ingest`,
`integration:manage`, ...), roles are just named bundles of permissions. **Enforce at the permission
level in code** (`@PreAuthorize("hasAuthority('repository:ingest')")`), not the role level — this
means adding a new role later (e.g. the deferred Engineering Manager persona from
`overview.md`) is a matter of bundling existing permissions differently, not touching
enforcement code anywhere.

**Common mistake:** checking `role == "ADMIN"` scattered through business logic. It's brittle
(hard to introduce fine-grained roles later) and it's exactly the kind of change that should be
data configuration, not a code change.

## 4. Identity Propagation

This is where security design meets the architecture from the previous three docs directly:

### 4.1 Gateway → Services (synchronous)
api-gateway validates the JWT signature and expiry once, then forwards identity to downstream
services via trusted internal headers (`X-User-Id`, `X-Org-Id`, `X-Permissions`) over the internal
network — downstream services trust these headers *only* because the network boundary ensures
they can only arrive via the gateway (services should not be independently reachable from outside
the cluster — enforce this at the infra/network-policy level, not just by convention). Downstream
services do not re-validate the JWT — that would mean every service needs the public key and
duplicate validation logic for no added security, since the gateway boundary already establishes
trust.

### 4.2 Services → Kafka Events
Extend the event envelope from `event-driven.md` Section 4 with an
`initiatedByUserId` and `orgId` field on events that originate from a user action (e.g.
`ingestion.repository.ingested` should carry who triggered the ingestion). This matters for two
reasons: audit trail (who caused what), and downstream consumers that need org scoping (e.g.
knowledge-service writing to Neo4j should tag nodes with `orgId` for later query-time scoping —
never rely on "we'll filter by org in the application layer" alone; tag the data itself).

Events that originate from the system itself (e.g. scheduler-service's cron-triggered sync) carry
a `SYSTEM` sentinel instead of a user ID — don't force a fake user context onto system-initiated
events.

### 4.3 Java (ai-gateway) → Python (AI services)
This boundary is different from internal Java-to-Java calls and deserves explicit treatment: the
Python services are a different runtime, likely a different trust zone in your cluster network
policy. ai-gateway should authenticate to Python services with a **service-level credential**
(e.g. a shared internal API key or mTLS, not the end user's JWT), while still passing `orgId` and
relevant scoping as request parameters — the Python service enforces org-scoped retrieval (e.g.
rag-service must not answer a question using another org's ingested code, ever) based on that
parameter, not based on trusting the caller to have already filtered correctly. Defense in depth:
even though ai-gateway should already scope requests correctly, the Python service enforcing scope
independently means one bug in ai-gateway doesn't leak cross-org data.

## 5. Secrets Management

Ariadne accumulates several categories of secret, each with different sensitivity:

| Secret type | Example | Where it lives |
|---|---|---|
| Platform secrets | JWT signing key, DB credentials | Environment/secret manager (not committed — `.env.example` has placeholders only, per your earlier `.gitignore` setup) |
| Per-org integration credentials | GitHub OAuth token, Slack webhook, Jira API token | Encrypted at rest in integration-service's datastore, never logged, never returned in API responses even to the owning org's admin (show "connected" status, not the token) |
| AI provider credentials | Ollama is local so N/A for v1; future hosted provider API key | Same as platform secrets — these are platform-level, not per-org, unless you later support bring-your-own-key |

**Why per-org integration credentials are the highest-risk category:** unlike platform secrets
(one set, tightly controlled), these multiply per organization and are managed through your own
application code (OAuth flows, token refresh), which means integration-service's credential
handling code is a genuine security-critical surface — worth extra review rigor, not treated like
routine CRUD code.

**Practical starting point for v1:** environment variables + `.env` (already gitignored) is fine
for local dev. Before any real deployment, move to a proper secret manager (cloud provider's
secret manager, or HashiCorp Vault if self-hosting) — flag this explicitly in the deployment doc
later as a hard requirement, not a nice-to-have, given what integration-service stores.

## 6. Data Privacy Consideration Specific to Ariadne

Worth stating explicitly since it shapes several decisions across these docs: **ingested source
code is the most sensitive data this platform handles**, more sensitive than the platform's own
user/auth data in most organizations' threat models. This is *why* `ai-architecture.md` Section
6 chose Ollama (local inference) for v1 rather than defaulting to a hosted LLM provider that would
mean sending proprietary code off-infrastructure. Keep this consideration in mind as a standing
constraint when making future AI/infra decisions, not just a one-time note.

---
## 8. Next Document

`docs/database.md` — moving out of pure architecture and into concrete
schema: PostgreSQL entity design per service (respecting the data-ownership boundaries from
`microservice.md`), Neo4j graph schema (node labels, relationship types), and
pgvector table design for embeddings.