# ADR-045 — Ejecución elegible real basada en `concept_assignment` y plan de cálculo

## Estado

Implementado

---

## Contexto

ADR-044 estableció la dirección técnica: abandonar servicios hardcodeados por concepto y ejecutar nómina mediante un grafo mínimo de conceptos configurados en base de datos. La primera iteración de ese diseño se implementó con un stub PoC en `CalculatePayrollUnitService.calculateEligibleReal()` que calculaba **únicamente el concepto "101"** mediante una llamada directa a `PayrollConceptGraphCalculator`.

Ese stub sirvió para validar las piezas del pipeline (convenio, binding, tablas, resolución temporal) pero nunca fue el diseño final. Para avanzar hacia una nómina real había que:

1. Determinar qué conceptos aplican a un empleado desde parametrización (no hardcode)
2. Expandir las dependencias transitivas necesarias para el cálculo
3. Construir un plan de ejecución en orden topológico
4. Ejecutar el plan completo, no solo un concepto
5. Persistir solo los conceptos con presencia en recibo

En paralelo, se habían diseñado las tablas `payroll_engine.concept_assignment` (elegibilidad por contexto) y el pipeline `BuildEligibleExecutionPlanUseCase` (construcción del plan), pero ninguno estaba enchufado al lanzador.

---

## Decisión

### 1. `concept_assignment` como fuente de elegibilidad canónica

La tabla `payroll_engine.concept_assignment` es la fuente oficial de qué conceptos aplican a un contexto dado:

| Columna | Semántica |
|---|---|
| `rule_system_code` | Ámbito del sistema de reglas |
| `concept_code` | Concepto elegible |
| `company_code` | Wildcardeable (null = aplica a todas las empresas) |
| `agreement_code` | Convenio aplicable |
| `employee_type_code` | Wildcardeable (null = aplica a todos los tipos) |
| `valid_from` / `valid_to` | Vigencia temporal |
| `priority` | Resolución de conflictos cuando hay múltiples asignaciones para el mismo concepto |

La elegibilidad se evalúa mediante `EmployeeAssignmentContext` (ruleSystemCode, companyCode, agreementCode, employeeTypeCode) en la fecha de referencia (fin de periodo).

**Divergencia de ADR-043**: ADR-043 proponía `payroll_object_activation` como mecanismo de activación de conceptos por contexto. Esa tabla existe en el modelo pero no fue el camino tomado para la ejecución elegible. `concept_assignment` es el mecanismo real en producción para el motor `payroll_engine`. `payroll_object_activation` queda para futuros casos de uso distintos si los hubiera.

### 2. Eliminación del stub PoC en `CalculatePayrollUnitService`

`calculateEligibleReal()` ya no hardcodea el concepto "101". En su lugar:

1. Obtiene el contexto de asignación del empleado desde `PayrollLaunchEligibleInputContext`
2. Llama a `BuildEligibleExecutionPlanUseCase.build(assignmentContext, periodEnd)`
3. Itera el plan resultante (`EligibleExecutionPlanResult.executionPlan()`) en orden topológico
4. Aplica la lógica de cálculo según `calculationType` de cada `ConceptExecutionPlanEntry`
5. Filtra por `payslipOrderCode != null` para decidir qué conceptos se persisten

### 3. Corrección del grafo de dependencias: edges de operandos

`DefaultConceptDependencyGraphService` ahora añade aristas `OPERAND_DEPENDENCY` para conceptos `RATE_BY_QUANTITY` y `PERCENTAGE`. Sin estas aristas, el grafo solo tenía aristas `FEED_DEPENDENCY`, lo que hacía que la ordenación topológica fuera incorrecta para conceptos con operandos de tipo `CONCEPT`.

Regla: un concepto `X` de tipo `RATE_BY_QUANTITY` cuyos operandos son `CONCEPT(D01)` y `CONCEPT(P01)` tiene dependencias `OPERAND_DEPENDENCY` de `X→D01` y `X→P01`. Estas aristas garantizan que D01 y P01 se calculen antes que X.

### 4. Expansión BFS con discovery por operandos

`DefaultEligibleConceptExpansionService` realiza la expansión del conjunto elegible en dos fases:

- **Feed-based discovery**: descubre conceptos adicionales accesibles por relaciones `FEED_DEPENDENCY`, pero solo incluye conceptos de tipo `PayrollObjectTypeCode.CONCEPT` (no tablas ni constantes).
- **Operand-based discovery**: para cada concepto con operandos de tipo `CONCEPT`, incluye los conceptos técnicos referenciados como operandos (ej. D01 y P01 para el concepto 101).

Los conceptos descubiertos por expansión (D01, P01) no están en `concept_assignment` y no son "elegibles" en sentido de negocio, pero son necesarios para el cálculo. El resultado distingue `eligibleConcepts` (asignados directamente) de `expandedConcepts` (conjunto completo incluyendo técnicos).

### 5. `payslipOrderCode` como filtro de persistencia

Un concepto calculado se persiste como línea de nómina si y solo si su `payslipOrderCode` no es null. El valor de `payslipOrderCode` determina además el orden de presentación en el recibo.

Los conceptos técnicos (D01, P01) tienen `payslipOrderCode = null` → se calculan pero no se persisten.  
Los conceptos de negocio (101, 970, 990) tienen `payslipOrderCode` establecido → se persisten y aparecen en el recibo.

### 6. Conceptos AGGREGATE 970, 980, 990

Se introducen tres conceptos de tipo `AGGREGATE`:

| Código | Mnemónico | Rol funcional | `payslipOrderCode` |
|---|---|---|---|
| 970 | TOTAL_DEVENGOS | `TOTAL_EARNING` | 970 |
| 980 | TOTAL_DEDUCCIONES | `TOTAL_DEDUCTION` | 980 |
| 990 | LIQUIDO_A_PAGAR | `NET_PAY` | 990 |

Sus fuentes provienen de relaciones `FEED_DEPENDENCY` desde los conceptos que los alimentan (ej. 101 → 970 y 101 → 990). La ejecución suma los importes de sus fuentes, aplicando inversión de signo si `invertSign = true`.

El concepto 980 (TOTAL_DEDUCCIONES) no se siembra en `concept_assignment` mientras no haya conceptos de deducción reales, ya que el plan builder lanzaría `MissingAggregateSourcesException` al no encontrar feed sources.

---

## Consecuencias

### Positivas

- El lanzador de nómina ya no contiene lógica de negocio específica por concepto. La parametrización en base de datos dicta completamente qué se calcula.
- Añadir un nuevo concepto elegible es solo insertar una fila en `concept_assignment` y definir las dependencias/fuentes correspondientes.
- La nómina persiste 970 y 990 además de 101, dando una vista real de devengos totales y líquido a pagar.
- El pipeline completo (elegibilidad → expansión → grafo → plan → ejecución) está cubierto por tests unitarios e integración E2E.

### Costes / Restricciones

- `concept_assignment` debe estar correctamente sembrado para cada convenio. Si está vacío, no se calcula ningún concepto (sin error implícito: la nómina se persistirá con 0 líneas).
- Los conceptos AGGREGATE con `concept_assignment` activo pero sin feed sources lanzarán `MissingAggregateSourcesException` en tiempo de construcción del plan.
- El modo `MINIMAL_REAL` queda retirado (`UnsupportedOperationException`). Solo `ELIGIBLE_REAL` y `FAKE` son modos operativos.

---

## Relación con ADRs previos

| ADR | Relación |
|---|---|
| ADR-036 | Define `CalculationType` — ahora todos los tipos (DIRECT_AMOUNT, RATE_BY_QUANTITY, PERCENTAGE, AGGREGATE) se ejecutan en el mismo dispatcher |
| ADR-038 | Define las relaciones FEED_DEPENDENCY — ahora usadas en tiempo de ejecución para AGGREGATE |
| ADR-039 | Define el grafo de dependencias — complementado con aristas OPERAND_DEPENDENCY |
| ADR-040 | Define el modelo conceptual de macro-grafo + activación + plan — este ADR documenta su implementación real |
| ADR-043 | Propuso `payroll_object_activation` como mecanismo de activación — desplazado por `concept_assignment` |
| ADR-044 | Inició la dirección del grafo mínimo real — este ADR completa y generaliza esa dirección |
