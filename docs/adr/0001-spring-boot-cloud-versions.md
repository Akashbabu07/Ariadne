# ADR-0001: Spring Boot 4.0.7 + Spring Cloud 2025.1.2

**Status:** Accepted
**Date:** 2026-07-24

## Context
All backend services need a consistent Spring Boot / Spring Cloud version pairing. Spring Cloud release trains are tied to specific Spring Boot versions, and mismatches cause dependency resolution failures.

## Decision
Standardize on **Spring Boot 4.0.7** with **Spring Cloud 2025.1.2** (Oakwood train) across every service and the root pom. Spring Cloud 2025.1.2 is the release confirmed compatible with Spring Boot 4.0.7 (and adds forward-compatibility with 4.1.0).

Java baseline: **21** (Spring Boot 4.x requires 17+; 21 is LTS and gives headroom).

## Alternatives Considered
- Spring Cloud 2025.1.0/2025.1.1 — earlier patches in the same train; 2025.1.1 first added Boot 4.0.1+ support but 2025.1.2 is the release explicitly validated against 4.0.7.
- Spring Cloud 2025.0.x — pinned to Spring Boot 3.5.x, incompatible with our Boot 4 choice.

## Consequences
- Root `pom.xml` and all per-service `pom.xml` files must be kept in sync on `spring-cloud.version`. (Note: services currently inherit directly from `spring-boot-starter-parent`, not from the root `atlas-backend` pom, so this value is duplicated rather than truly inherited — worth revisiting, see open question in `architecture/overview.md`.)
- Any future Spring Cloud upgrade must be checked against the compatibility matrix before bumping.
