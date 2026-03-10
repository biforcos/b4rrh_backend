# B4RRHH — Architecture Decision Records

Este documento recoge decisiones de arquitectura ya tomadas o en discusión formal.

## Cómo usar este documento

Formato sugerido por decisión:

```md
## ADR-XXX — Título
- Estado: proposed | accepted | superseded | rejected
- Fecha: YYYY-MM-DD
- Contexto:
- Decisión:
- Consecuencias:
```

---

## ADR-001 — Identidad lógica del empleado incluye `employee_type`
- Estado: proposed
- Fecha: 2026-03-10
- Contexto:
  El modelo actual identifica al empleado principalmente mediante `(rule_system_code, employee_number)`.
  Sin embargo, se ha detectado un caso relevante: dentro de un mismo `rule_system` pueden coexistir personas con el mismo número pero con distinto tipo de empleado, por ejemplo `EMP`, `EXT` o `JUB`.
  Ignorar esa dimensión era aceptable en una PoC pequeña, pero a medida que el proyecto crece aumenta el coste de corregirlo tarde.
- Decisión:
  Se propone elevar `employee_type` a parte explícita de la identidad lógica del empleado.
  La opción preferida es:

  `(rule_system_code, employee_type, employee_number)`

  y tratar cualquier otra representación como derivada o técnica.
- Consecuencias:
  - Habrá que revisar business keys, constraints e índices.
  - Los endpoints y contratos de API deberán poder expresar `employee_type`.
  - El catálogo de recursos y la documentación de dominio deberán reflejar esta identidad.
  - Conviene tomar esta decisión antes de ampliar mucho más el número de recursos.
- Estado de siguiente revisión:
  Pendiente de aceptación formal tras revisar impacto técnico.

## ADR-002 — Separar backlog, decisiones y catálogo de dominio
- Estado: accepted
- Fecha: 2026-03-10
- Contexto:
  El proyecto ya no es una simple PoC lineal. Aparecen ideas laterales, decisiones estructurales y cambios reales de modelo. Si todo se mezcla en conversación o en código, el contexto se degrada rápido.
- Decisión:
  Mantener tres artefactos distintos:
  - `backlog.md` para ideas, mejoras, bugs y líneas de trabajo pendientes
  - `architecture-decisions.md` para decisiones de arquitectura
  - catálogo de dominio como fuente de verdad del modelo
- Consecuencias:
  - Se reduce la pérdida de contexto.
  - Resulta más fácil retomar discusiones semanas después.
  - Se puede evolucionar sin necesidad de meter ya un gestor completo de tickets.
