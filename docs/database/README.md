# Database Documentation

One subfolder per service that owns its own schema, e.g. `database/auth-service/schema.md`.

Only `auth-service` currently declares a database dependency (`postgresql`, runtime scope). As other services add persistence, document here:
- Schema / ER diagram
- Migration tool choice (Flyway/Liquibase — not yet added to any pom)
- Ownership boundaries (which service is the source of truth for which data)
