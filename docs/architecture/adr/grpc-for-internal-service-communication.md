# ADR  Use gRPC for internal service-to-service communication

## Status
Accepted

## Context
ATLAS has multiple backend services (Java/Spring) and multiple AI services (Python)
that will need to call each other's business logic directly (not just their own DB).
REST/JSON via OpenFeign works for simple cases but is heavier and has no native
streaming support, which matters for cross-language calls and long-running AI operations.

## Decision
- External traffic (browser → api-gateway → services): stays REST/JSON.
- Internal service-to-service calls where performance, streaming, or cross-language
  (Java ↔ Python) contracts matter: use gRPC with shared `.proto` definitions.
- OpenFeign remains acceptable for simple, low-frequency, same-language internal calls
  where gRPC's setup cost isn't justified.

## First planned application
- `ai-gateway` → Python AI services (parser-service, embedding-service, rag-service,
  reasoning-service): gRPC, since this is the clearest cross-language, high-frequency boundary.
- `analysis-service` → `graph-service`: gRPC, if traversal query volume justifies it.

## Consequences
- Adds Protobuf toolchain (`protoc`, Maven/Gradle gRPC plugins, Python grpcio) once
  the first gRPC call is built.
- Two communication protocols exist in the system (REST + gRPC) — deliberate, not
  accidental complexity, scoped strictly to internal service calls.