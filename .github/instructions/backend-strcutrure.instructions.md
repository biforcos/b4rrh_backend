---
applyTo: "**/*.java"
---

## Java structure rules

- Organize code by bounded business capability first, and by layer second, if the package structure remains clear.
- A typical feature may include:
  - domain
  - application
  - infrastructure
- Domain contains entities, value objects, domain services, and domain rules.
- Application contains use cases, commands, queries, input ports, output ports, and orchestrating services.
- Infrastructure contains REST controllers, persistence entities, Spring Data repositories, mappers, and configuration.

## Forbidden patterns

- No controller-to-repository direct calls.
- No business logic in controllers.
- No business logic in JPA entities.
- No direct dependency from domain to Spring or persistence code.
- No exposing persistence entities outside infrastructure.
- No anemic "service utils" dumping ground.

## Preferred patterns

- One use case per clear business action.
- Explicit command/query models.
- Explicit mapper classes at boundaries.
- Value objects when they clarify meaning.
- Domain invariants enforced close to the domain.