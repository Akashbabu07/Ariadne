# Roadmap

## Phase 0 — Foundation (current)
- [x] Scaffold 11 Spring Boot services + root Maven pom
- [x] Align Spring Boot/Cloud versions across services (ADR-0001)
- [ ] Decide service discovery approach
- [ ] Decide sync vs async inter-service communication
- [ ] Add `shared` module for common DTOs/error handling
- [ ] Stand up local infra (docker-compose: Postgres, message broker if needed)
- [ ] Add frontend project

## Phase 1 — First vertical slice
- [ ] auth-service: real registration/login/JWT issuance
- [ ] api-gateway: route to auth-service, forward JWT
- [ ] project-service: minimal CRUD to validate the pattern end-to-end

## Phase 2 — Core domain services
- [ ] ingestion-service, knowledge-service, search-service

## Phase 3 — AI/analysis layer
- [ ] ai-orchestrator, analysis-service, graph-service

## Phase 4 — Operational maturity
- [ ] integration-service, scheduler-service
- [ ] Observability (logging, metrics, tracing across services)
- [ ] CI/CD pipeline
