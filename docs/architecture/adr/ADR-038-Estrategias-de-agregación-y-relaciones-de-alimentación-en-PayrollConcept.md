# ADR-038 — Estrategias de agregación y relaciones de alimentación en `PayrollConcept`

## Estado
Propuesto

---

## Contexto

El ADR-036 define las tipologías canónicas de cálculo:

- `DIRECT_AMOUNT`
- `RATE_BY_QUANTITY`
- `PERCENTAGE`
- `AGGREGATE`

El ADR-037 define la resolución de operandos mediante sources tipados.

Sin embargo, el tipo `AGGREGATE` requiere una definición adicional:

- cómo se determinan los conceptos que participan en el agregado;
- dónde reside la responsabilidad de dicha pertenencia;
- cómo evitar modelos frágiles basados en listas manuales.

En nómina real existen dos patrones claramente diferenciados:

1. Bases o acumulados donde cada concepto decide si participa  
2. Totales o subtotales donde la pertenencia se deriva automáticamente

Es necesario modelar ambas realidades sin mezclarlas.

---

## Decisión

Se definen dos estrategias canónicas de agregación para `AGGREGATE`:

- `FEED_BY_SOURCE`
- `SELECT_BY_RULE`

Estas estrategias determinan cómo se construye el conjunto de miembros del agregado.

---

## Principio rector

La pertenencia a un agregado puede definirse:

- desde el concepto origen (semántica declarativa del concepto)
- o desde el agregado destino (regla de selección)

Ambas aproximaciones son necesarias y no son equivalentes.

---

## Definición de `AGGREGATE`

Un `AGGREGATE` es un concepto cuyo resultado se obtiene combinando resultados de otros conceptos ya calculados.

### Operación inicial soportada
- `SUM`

---

## Estrategias de membership

---

### 1. `FEED_BY_SOURCE`

#### Definición
La pertenencia al agregado se declara en el concepto origen.

#### Modelo conceptual
Cada concepto define a qué agregados alimenta.

#### Ejemplo
- `SALARIO_BASE` alimenta `BASE_CC`
- `PLUS_TRANSPORTE` no alimenta `BASE_CC`
- `PRORRATA_EXTRA` alimenta `BASE_IRPF`

---

#### Motivación

La semántica relevante en muchos casos pertenece al concepto:

- cotiza / no cotiza
- tributa / no tributa
- alimenta base / no alimenta

Esta información es intrínseca al concepto, no al agregado.

---

#### Resolución en runtime

Para calcular un agregado:

1. se evalúan todos los conceptos;
2. se seleccionan aquellos con relación activa hacia el target;
3. se combinan según `feedMode`.

---

#### Relación de alimentación

Se introduce la relación conceptual:

### `ConceptFeedRelation`

Campos mínimos:

- `sourceConceptCode`
- `targetObjectCode`
- `feedMode`
- `feedValue` (opcional)
- `effectiveFrom`
- `effectiveTo`

---

#### Modos iniciales

- `INCLUDE` → aporta el 100% del importe
- `PERCENTAGE` → aporta un porcentaje del importe

---

#### Uso recomendado

- bases de cotización
- bases fiscales
- acumulados técnicos
- provisiones
- cualquier agregado donde la pertenencia dependa del concepto origen

---

#### Ventajas

- semántica clara y localizada;
- menor riesgo de omisiones al introducir nuevos conceptos;
- alineación con lógica de negocio real.

---

#### Costes

- la composición del agregado no es visible directamente desde el destino;
- requiere resolución inversa en runtime.

---

---

### 2. `SELECT_BY_RULE`

#### Definición
La pertenencia al agregado se define mediante una regla en el propio agregado.

---

#### Modelo conceptual
El agregado define una condición de selección sobre el conjunto de conceptos.

---

#### Ejemplos

- `TOTAL_DEVENGOS` → todos los conceptos con `functionalNature = EARNING`
- `TOTAL_DEDUCCIONES` → todos los conceptos con `functionalNature = DEDUCTION`

---

#### Motivación

Existen agregados cuya composición:

- no debe mantenerse manualmente;
- debe adaptarse automáticamente a nuevos conceptos;
- depende de la naturaleza funcional, no de decisiones individuales.

---

#### Parametrización mínima

- `selectionRuleType`
- `selectionRuleValue`

---

#### Reglas iniciales soportadas

- `BY_FUNCTIONAL_NATURE`
- `BY_FUNCTIONAL_SUBNATURE`
- `BY_EXPLICIT_CONCEPT_LIST`

---

#### Uso recomendado

- totales de recibo
- subtotales funcionales
- agrupaciones lógicas
- bloques de presentación

---

#### Ventajas

- evita mantenimiento manual;
- escala automáticamente con nuevos conceptos;
- reduce riesgo de errores por omisión.

---

#### Costes

- menor control individual por concepto;
- requiere definición clara de taxonomías funcionales.

---

## Regla clave de diseño

No se modelará `AGGREGATE` como una lista fija de miembros en todos los casos.

---

## Criterios de uso

| Tipo de agregado        | Estrategia recomendada |
|------------------------|------------------------|
| Bases (cotización)     | FEED_BY_SOURCE         |
| Bases (fiscalidad)     | FEED_BY_SOURCE         |
| Acumulados técnicos    | FEED_BY_SOURCE         |
| Totales funcionales    | SELECT_BY_RULE         |
| Subtotales             | SELECT_BY_RULE         |

---

## Interacción con otros ADR

- ADR-036 define el tipo `AGGREGATE`
- ADR-037 define cómo se resuelven operandos
- Este ADR define cómo se resuelven los miembros

---

## Consecuencias

### Positivas

- modelo robusto frente a crecimiento del catálogo;
- separación clara de responsabilidades;
- alineación con lógica real de nómina;
- soporte tanto para control fino como para automatización.

---

### Costes

- mayor complejidad conceptual;
- necesidad de implementar dos estrategias en runtime;
- necesidad de definir correctamente `functionalNature`.

---

## No objetivos

Este ADR no define:

- ejecución del motor de cálculo;
- orden de evaluación de conceptos;
- resolución de conflictos entre feeds;
- filtros avanzados o condiciones complejas;
- modelo físico de persistencia.

---

## Resumen ejecutivo

Se establecen dos estrategias complementarias para la construcción de agregados:

- `FEED_BY_SOURCE`: la pertenencia se declara en el concepto origen  
- `SELECT_BY_RULE`: la pertenencia se define mediante reglas en el agregado

Ambas estrategias son necesarias para modelar correctamente:

- bases técnicas (controladas por concepto)
- totales funcionales (derivados automáticamente)

Este modelo evita listas manuales frágiles y permite construir un motor de nómina flexible, escalable y alineado con el dominio.