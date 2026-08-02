# Ariadne — AI Architecture

Status: Draft
Owner: (you)
Last updated: 2026-07-25
Depends on: `overview.md`, `microservice.md`, `event-driven.md`

## 1. Purpose of This Document

The tech stack lists Spring AI, LangChain, LangGraph, Sentence Transformers, and Ollama — five
separate AI-adjacent technologies. Without a clear document like this, it's easy for "AI features"
to become a grab-bag of LLM calls scattered wherever they're convenient. This document draws the
actual boundaries: what ai-gateway does versus what each Python service does, how RAG context gets
assembled from *two* different retrieval sources (graph + vector, not just vector), how
reasoning-service's agentic workflows differ from simple RAG, and where model/provider choice is
allowed to change without touching calling code.

## 2. Why an AI Gateway at All

Per `microservice.md` Section 3.10: ai-gateway exists so no Java service talks to a
Python AI service directly. Restating why that matters specifically for AI, not just as a general
microservice rule:

- **Model/provider churn is guaranteed.** You're on Ollama now; you will almost certainly add a
  hosted provider later for quality or scale reasons. If five Java services each call Python
  services directly, that's five places to update. With ai-gateway, it's one.
- **AI calls need cross-cutting handling that's different from normal REST calls**: timeout
  tolerances are longer, retry semantics are different (don't blindly retry a $0.02 LLM call the
  way you'd retry a cheap DB read), and you likely want centralized token/cost tracking. That
  belongs in one place, not duplicated per caller.
- **Prompt/context construction is a legitimate piece of domain logic in its own right** — it's
  not just "forward the request." Centralizing it means prompt strategy can evolve without hunting
  through every service that happens to call an LLM.

## 3. AI Gateway Responsibilities (and non-responsibilities)

**Does:**
- Exposes a small set of stable internal APIs to the rest of the Java backend: e.g.
  `POST /ai/chat` (routes to rag-service), `POST /ai/analyze/impact` (routes to reasoning-service),
  `POST /ai/embed` (routes to embedding-service, mainly used internally by other AI flows).
- Routes to the correct Python service based on request type.
- Applies cross-cutting concerns: auth passthrough (validates the caller's already-validated JWT
  claims, doesn't re-auth), timeout/retry policy tuned for AI workloads, request/response logging
  for cost and quality monitoring.
- Owns the Spring AI integration layer — if Spring AI's abstractions are used for provider-agnostic
  model calls anywhere in the Java side, they live here, not scattered elsewhere.

**Does not:**
- Does not itself build RAG context or run LangGraph workflows — that's Python-side logic
  (Section 4, 5). ai-gateway is a coordinator, not where AI logic lives. Keeping it thin means the
  actual AI engineering (prompt strategy, retrieval strategy, agent design) stays in Python where
  the ecosystem (LangChain/LangGraph) is strongest, rather than being awkwardly reimplemented or
  half-duplicated in Java.

## 4. RAG Service — Context Assembly

This is the part worth being precise about, because "RAG" often gets treated as "vector search +
LLM call" when Ariadne's actual advantage is that it has **two** retrieval sources, not one:

1. **Vector search** (via embedding-service / pgvector, fronted by search-service) — semantically
   similar code/doc chunks to the question.
2. **Graph traversal** (via graph-service) — structurally *related* entities that may not be
   semantically similar in text but matter architecturally. Example: a question about "what breaks
   if I change this auth method" needs the dependency graph, not just text similarity — nothing in
   the calling code may textually resemble the word "auth."

**Context assembly flow for a question:**
1. rag-service receives the question (via ai-gateway).
2. Runs vector search for semantically relevant chunks.
3. Extracts entity references from top vector results (e.g. which service/class/function they
   belong to) and queries graph-service for structurally related entities (dependents, dependencies)
   around those same entities.
4. Merges both result sets into a single context window, deduplicated, with a clear budget (token
   limit) split between the two sources — don't let vector results alone crowd out graph context or
   vice versa; this ratio is a tuning knob worth exposing as config, not hardcoding.
5. Builds the final prompt with retrieved context + question, calls the LLM (via LangChain), returns
   the answer with **citations back to source entities** (repository/file/graph-node references) —
   not just prose. Citations matter here specifically because the target users (Section 3 of the
   system overview) are engineers who need to verify an AI answer against real code, not just accept
   it — a chatbot answer engineers can't trace back to source is close to worthless for this
   audience.

**Common mistake:** treating this as pure vector RAG because that's the default LangChain tutorial
pattern. For a codebase-understanding tool, the graph is often more informative than text similarity
— this hybrid retrieval is the actual differentiator between Ariadne and "ChatGPT with your code
pasted in," and it's worth protecting that design intent explicitly here so it doesn't quietly erode
into vector-only RAG under implementation pressure.

## 5. Reasoning Service — Agentic Workflows (LangGraph)

Distinct from rag-service (Section 4) because these are **multistep, stateful workflows**, not
single-shot question answering:

- **Impact analysis**: given "if I change X, what's affected" — this isn't one retrieval + one LLM
  call. It's: traverse the dependency graph (via graph-service) → for each affected node, assess
  materiality (does this look like a breaking change or cosmetic) → possibly recurse further if a
  dependent is itself widely depended upon → synthesize a ranked impact report. That's a graph of
  reasoning steps, which is exactly what LangGraph is for (as opposed to LangChain's more linear
  chains).
- **Architecture drift detection**: compare the *intended* architecture (from docs/ADRs, if
  ingested) against the *actual* architecture (from the knowledge graph) — a workflow with
  comparison and judgment steps, not a single retrieval.
- **Duplicate detection**: find semantically similar code across the graph that may indicate
  duplicated logic — combines vector similarity (candidate generation) with a reasoning step to
  judge whether candidates are true duplicates or coincidentally similar (a step pure vector
  similarity can't make on its own).

**Why this must be a separate service from rag-service** (restating and grounding
`microservice.md` Section 4.4 now that the actual workflows are concrete): these
workflows can run for seconds to minutes, involve multiple LLM calls and multiple graph-service
round-trips per single "answer," and should not share a request-handling path or resource pool with
rag-service's simple, fast, single-shot chat responses. Mixing them risks slow reasoning workflows
starving latency-sensitive chat requests.

## 6. Model/Provider Abstraction

- Ollama (local) for v1 — deliberate choice for cost and data-locality during development (source
  code never leaves your infrastructure, which matters given Ariadne ingests proprietary codebases).
- Abstract the model call behind LangChain's model interface on the Python side (and Spring AI's on
  the Java side, for anything Java-side that calls a model directly, if that ever happens) so
  swapping to a hosted provider later, or offering provider choice per deployment (self-hosted
  customers might mandate Ollama-only, for instance, given the code-privacy angle), doesn't require
  rewriting call sites.
- Track which model/version generated which response (extend the event/response envelope in
  `event-driven.md` Section 4 style — a `modelId` field) — needed for reproducing
  or debugging a bad answer later, and for cost tracking if/when you add a hosted provider.

## 7. Evaluation (worth planning even before v1 ships)

AI features are the one part of this system where "it compiles and the demo looks good" is not
sufficient evidence of quality — answers can be plausible-sounding and wrong. At minimum, plan for:
- A small, hand-curated set of question/expected-answer(or expected-citation) pairs per repo used
  in testing, checked whenever rag-service's retrieval or prompt strategy changes.
- Logging retrieval sources per answer (which vector chunks, which graph nodes) so bad answers are
  debuggable after the fact — this is a direct consumer of the `correlationId` tracing pattern from
  `event-driven.md`.

This doesn't need to be a full eval framework for v1, but the logging/traceability hooks are cheap
to add now and expensive to retrofit — consistent with the pattern in every doc so far.
---
## 9. Next Document

`docs/architecture/05-security-architecture.md` — auth-service's JWT/RBAC model in detail, how
identity propagates through the gateway to downstream services (including into Kafka events and
across the Java→Python ai-gateway boundary), and secrets management for the various external
integrations (GitHub tokens, Slack, Jira, LLM provider keys).