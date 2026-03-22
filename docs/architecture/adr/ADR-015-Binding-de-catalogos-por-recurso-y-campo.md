# ADR-015 — Binding de catálogos por recurso y campo

## Estado
Propuesto

## Contexto
B4RRHH ya dispone de un metamodelo funcional de catálogos en el bounded context `rulesystem`:
- `rule_system`
- `rule_entity_type`
- `rule_entity`

Además:
- ya existen endpoints de `rule_entity` filtrables por business keys;
- ya existe un caso dependiente real para edición: `GET /labor-classification-catalog/agreement-categories`;
- `rule_entity_type` debe nombrar conceptos funcionales reutilizables (por ejemplo `COMPANY`, `WORK_CENTER`, `COST_CENTER`);
- frontend no debe asumir semántica de metamodelo compleja ni convertirse en renderizador genérico de formularios.

El problema de producto actual tiene dos necesidades simultáneas:
1. mostrar labels/literales visibles en frontend, no solo códigos;
2. conocer qué catálogo aplica a un campo concreto de un recurso para pedir opciones válidas por `rule_system`.

## Problema
Hoy, el sistema valida códigos de catálogo en verticales concretos, pero no existe un diccionario backend explícito y reusable que responda de forma simple:
- qué catálogo corresponde a cada campo;
- si ese catálogo se resuelve de forma directa, dependiente o custom.

Sin este diccionario aparecen dos riesgos no deseados:
- construir un "GET de la muerte" que devuelva todas las `rule_entities` para que frontend infiera todo;
- codificar manualmente vertical por vertical y campo por campo en Angular.

## Decisión
Se adopta una solución backend-first, pequeña y evolutiva basada en binding recurso/campo -> catálogo aplicable.

### Decisiones fijadas
1. Introducir la tabla `resource_field_catalog_binding`.
2. Clasificar bindings en `DIRECT`, `DEPENDENT`, `CUSTOM`.
3. El binding define **qué catálogo aplica**, no **cómo renderizar formularios**.
4. Para lectura, preferir read models enriquecidos con `code + name`.
5. Para edición, frontend consulta bindings y consume opciones directas o endpoints específicos según el caso.
6. No introducir en esta fase un motor universal de dependencias.
7. No introducir un endpoint masivo de todas las `rule_entities`.

## Diseño Propuesto
### Persistencia
Tabla: `rulesystem.resource_field_catalog_binding`

Campos:
- `resourceCode`
- `fieldCode`
- `ruleEntityTypeCode`
- `catalogKind` (`DIRECT` | `DEPENDENT` | `CUSTOM`)
- `dependsOnFieldCode`
- `customResolverCode`
- `active`
- `createdAt`
- `updatedAt`

Identidad funcional recomendada:
- `resourceCode + fieldCode`.

Relación recomendada:
- `ruleEntityTypeCode` referencia por business key a `rule_entity_type.code` cuando aplique.

### Reglas de consistencia
- `DIRECT` => `ruleEntityTypeCode` obligatorio y `dependsOnFieldCode` nulo.
- `DEPENDENT` => `ruleEntityTypeCode` obligatorio y `dependsOnFieldCode` obligatorio.
- `CUSTOM` => `customResolverCode` obligatorio.

### Semántica de resolución
- `DIRECT`: opciones por `ruleSystemCode + ruleEntityTypeCode`.
- `DEPENDENT`: opciones por endpoint específico del caso de negocio.
- `CUSTOM`: resolución específica controlada por backend, explícita por `customResolverCode`.

## API Propuesta (primera iteración)
### 1) Consultar bindings de un recurso
`GET /catalog-bindings/{resourceCode}`

Respuesta mínima:
- `resourceCode`
- `bindings[]` con:
  - `fieldCode`
  - `catalogKind`
  - `ruleEntityTypeCode` (nullable)
  - `dependsOnFieldCode` (nullable)
  - `customResolverCode` (nullable)
  - `active`

### 2) Obtener opciones de catálogo directo
`GET /catalog-options/direct?ruleSystemCode=...&ruleEntityTypeCode=...&referenceDate=...&q=...`

Respuesta mínima:
- `items[]` con:
  - `code`
  - `name`
  - `active`
  - `startDate`
  - `endDate`

### 3) Casos dependientes
Mantener endpoints específicos cuando compense (por ejemplo `labor-classification-catalog/agreement-categories`).

## Reglas de Uso
### Lectura
Backend enriquece read models con labels visibles sin delegar inferencias al frontend.

Ejemplos:
- `workCenterCode` + `workCenterName`
- `agreementCode` + `agreementName`
- `agreementCategoryCode` + `agreementCategoryName`

### Edición
Frontend:
1. consulta binding por `resourceCode`;
2. para `DIRECT`, pide opciones directas por `ruleSystemCode`;
3. para `DEPENDENT`/`CUSTOM`, usa endpoint específico del caso.

No se pretende frontend dinámico universal.

## No Objetivos
Este ADR no pretende resolver todavía:
- un form builder genérico;
- un motor universal de dependencias entre campos;
- inferencia automática de UI desde metamodelo completo;
- un endpoint masivo que exponga todas las `rule_entities` para que frontend deduzca semántica;
- relajar reglas de vertical-first o mover lógica de dominio a Angular.

## Consecuencias
### Positivas
- Evita acoplamiento manual campo a campo en frontend.
- Mantiene el control semántico en backend.
- Permite crecimiento incremental por verticales sin arquitectura astronauta.
- Reutiliza metamodelo existente y business keys.

### Costes
- Introduce una tabla más en metamodelo de consumo.
- Requiere gobierno de seeds de bindings.
- Exige disciplina para distinguir `DIRECT` vs `DEPENDENT` vs `CUSTOM`.

## Casos Iniciales
Bindings iniciales a registrar:
- `employee.presence` / `companyCode` -> `COMPANY` (`DIRECT`)
- `employee.work_center` / `workCenterCode` -> `WORK_CENTER` (`DIRECT`)
- `employee.cost_center` / `costCenterCode` -> `COST_CENTER` (`DIRECT`)
- `employee.contact` / `contactTypeCode` -> `EMPLOYEE_CONTACT_TYPE` (`DIRECT`)
- `employee.identifier` / `identifierTypeCode` -> `EMPLOYEE_IDENTIFIER_TYPE` (`DIRECT`)
- `employee.address` / `addressTypeCode` -> `EMPLOYEE_ADDRESS_TYPE` (`DIRECT`)
- `employee.labor_classification` / `agreementCode` -> `AGREEMENT` (`DIRECT`)
- `employee.labor_classification` / `agreementCategoryCode` -> `AGREEMENT_CATEGORY` (`DEPENDENT`, `dependsOnFieldCode=agreementCode`)

## Plan por Fases
### Fase 1
- Crear tabla `resource_field_catalog_binding` con restricciones de consistencia.
- Seed inicial de casos `DIRECT` y caso `DEPENDENT` de labor classification.
- Exponer `GET /catalog-bindings/{resourceCode}`.

### Fase 2
- Exponer `GET /catalog-options/direct`.
- Empezar enriquecimiento `code + name` en read models prioritarios (work center, labor classification).

### Fase 3
- Integrar consumo en frontend para edición guiada por binding.
- Extender gradualmente a más verticales y consolidar endpoints dependientes puntuales.

## Riesgos a Evitar
- Convertir el binding en framework genérico de formularios.
- Duplicar semántica de negocio en frontend.
- Diseñar una API universal compleja antes de validar casos reales.
- Introducir IDs técnicos en contratos públicos.
- Romper vertical-first moviendo reglas de dominio fuera de sus verticales.
