# API Documentation

One subfolder per service once endpoints exist, e.g. `api/auth-service/openapi.yaml`.

Suggested approach: use springdoc-openapi (already pinned in root `pom.xml` at `2.8.9`) to auto-generate OpenAPI specs from each service, then export them here for versioned, reviewable snapshots — rather than hand-writing specs from scratch.

## Conventions (proposed)
- All endpoints versioned under `/api/v1/...`
- Auth via `Authorization: Bearer <jwt>` issued by auth-service
- Error responses follow a consistent shape (TBD — candidate for ADR)
