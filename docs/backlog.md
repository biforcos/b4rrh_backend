# B4RRHH — Technical Backlog

Este documento recoge ideas, dudas, mejoras y líneas de trabajo que no conviene perder de vista.

## Cómo usar este backlog

Cada entrada debería indicar, cuando sea posible:

- **Tipo**: arquitectura, modelo, validación, API, persistencia, DX, bug, exploración
- **Estado**: idea, analizando, decidido, implementando, hecho, descartado
- **Impacto**: bajo, medio, alto
- **Notas**: contexto mínimo para recordar por qué existe

Formato sugerido:

```md
## [ID] Título corto
- Tipo:
- Estado:
- Impacto:
- Contexto:
- Siguiente paso:
```

---

## Backlog inicial

## [BL-001] Incluir `employee_type` en la identidad del empleado
- Tipo: arquitectura / modelo
- Estado: analizando
- Impacto: alto
- Contexto: el identificador actual `(rule_system_code, employee_number)` puede quedarse corto si existen varios tipos de empleado con el mismo número dentro del mismo sistema (por ejemplo `EMP`, `EXT`, `JUB`).
- Siguiente paso: decidir si la clave lógica pasa a ser `(rule_system_code, employee_type, employee_number)` o una variante equivalente.

## [BL-002] Revisar impacto de `employee_type` en todo el metamodelo
- Tipo: arquitectura / refactor
- Estado: idea
- Impacto: alto
- Contexto: si `employee_type` entra en la identidad lógica, hay que revisar catálogos, endpoints, constraints, búsquedas, DTOs, imports y naming.
- Siguiente paso: inventario de puntos afectados.

## [BL-003] Sembrar varios `rule_systems` en datos de prueba
- Tipo: datos / pruebas
- Estado: idea
- Impacto: medio
- Contexto: la PoC valida bien con un único `rule_system`, pero conviene tensionar el modelo con varios sistemas para detectar supuestos ocultos.
- Siguiente paso: preparar una V2 de datos de ejemplo o de migraciones Flyway.

## [BL-004] Validaciones genéricas de temporalidad
- Tipo: validación / framework
- Estado: idea
- Impacto: alto
- Contexto: recursos historizados como `employee.presence`, `employee.contract` o `employee.address` requieren reglas repetibles de no solape, cierre y ocurrencias activas.
- Siguiente paso: extraer reglas genéricas apoyadas en el metamodelo.

## [BL-005] Explorar `rule_entity_type` como pieza de validación real
- Tipo: arquitectura / validación
- Estado: idea
- Impacto: alto
- Contexto: puede servir para gobernar correspondencias campo↔valores permitidos y reforzar validaciones de dominio de forma homogénea.
- Siguiente paso: bajar un caso real completo y comprobar si compensa la complejidad.

## [BL-006] Definir política de errores funcionales vs técnicos
- Tipo: API / backend
- Estado: idea
- Impacto: medio
- Contexto: a medida que aparezcan más validaciones conviene distinguir claramente 4xx funcionales de 5xx técnicos.
- Siguiente paso: acordar convención de códigos y payloads de error.

## [BL-007] Investigar error 500 de la implementación actual
- Tipo: bug
- Estado: pendiente de análisis
- Impacto: alto
- Contexto: existe al menos un error 500 mencionado durante el desarrollo y conviene capturarlo para no perderlo.
- Siguiente paso: documentar escenario exacto, endpoint, payload y stacktrace cuando se tenga a mano.

## [BL-008] Convención estable de claves de negocio y claves técnicas
- Tipo: arquitectura / persistencia
- Estado: idea
- Impacto: medio
- Contexto: cuanto antes se cierre una convención, menos dolor habrá en refactors posteriores.
- Siguiente paso: definir plantilla por recurso: business key, surrogate key, unique constraints e índices.

## [BL-009] Matriz recurso → restricciones de simultaneidad
- Tipo: modelo / documentación
- Estado: idea
- Impacto: medio
- Contexto: el catálogo ya refleja `SINGLE_ACTIVE` y `MULTIPLE_ACTIVE`, pero conviene convertirlo en una matriz operacional para validaciones y tests.
- Siguiente paso: derivar una tabla ejecutable o fácilmente verificable.

## [BL-010] Estrategia de evolución de la PoC a framework
- Tipo: arquitectura
- Estado: idea
- Impacto: alto
- Contexto: el proyecto empieza a parecer más un framework de dominio HR que una API puntual.
- Siguiente paso: explicitar qué partes son genéricas y cuáles son específicas del caso `employee`.

---

## Cola rápida

Ideas muy cortas que todavía no merecen ficha propia:

- Revisar naming de recursos antes de consolidar endpoints públicos.
- Valorar plantillas para generar CRUD + validaciones a partir del catálogo.
- Definir checklist para añadir un nuevo recurso al dominio.
