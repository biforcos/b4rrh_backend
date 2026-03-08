---
applyTo: "**/*Entity.java,**/*Repository.java,**/db/migration/*.sql"
---

## Persistence and migration rules

- JPA entities are persistence artifacts, not domain entities.
- Keep entity mappings simple and explicit.
- Prefer surrogate numeric primary keys for technical identity.
- Business identifiers must be modeled explicitly and separately.
- History tables and assignment tables should reflect temporal validity when required.
- Be cautious with cascade settings.
- Avoid orphanRemoval unless the lifecycle is unquestionably owned and approved.
- Use Flyway SQL migrations with clear names.
- Every schema change must be justified by an approved business need.
- Do not create schema elements speculatively.