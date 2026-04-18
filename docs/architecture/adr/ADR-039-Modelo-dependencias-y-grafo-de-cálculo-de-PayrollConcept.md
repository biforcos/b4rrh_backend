# ADR-039 — Modelo de dependencias y grafo de cálculo de `PayrollConcept`

## Estado
Propuesto

---

## Contexto

Los ADR previos han establecido:

- ADR-036 — Tipologías canónicas de cálculo (`DIRECT_AMOUNT`, `RATE_BY_QUANTITY`, `PERCENTAGE`, `AGGREGATE`)
- ADR-037 — Resolución de operandos mediante sources tipados
- ADR-038 — Estrategias de agregación (`FEED_BY_SOURCE`, `SELECT_BY_RULE`)

Estas decisiones permiten que un `PayrollConcept`:

- consuma resultados de otros conceptos (`source = CONCEPT`);
- participe en agregados;
- sea utilizado como base o componente de otros cálculos.

Como consecuencia, el conjunto de conceptos deja de ser independiente y pasa a formar una red de relaciones.

Es necesario formalizar esta red como un **modelo explícito de dependencias**, base para cualquier estrategia de ejecución posterior.

---

## Decisión

Se define un modelo explícito de dependencias entre `PayrollConcept` y su representación como un **grafo dirigido de cálculo**.

---

## Definición de dependencia

Se establece que:

> Un `PayrollConcept` A depende de otro concepto B si el cálculo de A requiere que B haya sido previamente calculado.

Esta relación se representa como una arista dirigida:


B → A


donde B debe evaluarse antes que A.

---

## Tipos de dependencia

Se definen tres tipos canónicos de dependencia.

---

### 1. `OPERAND_DEPENDENCY`

#### Definición
Se produce cuando un operando de un concepto se resuelve mediante `source = CONCEPT`.

#### Ejemplos
- `SALARIO_BASE` depende de `DIAS_PRESENCIA`
- `SALARIO_BASE` depende de `PRECIO_DIA`
- `IRPF` depende de `BASE_IRPF`
- `IRPF` depende de `TIPO_IRPF_EFECTIVO`

#### Origen
ADR-037 — Sources y resolución de operandos

#### Naturaleza
- explícita
- declarada directamente en la configuración del concepto

---

### 2. `FEED_DEPENDENCY`

#### Definición
Se produce cuando un `AGGREGATE` con estrategia `FEED_BY_SOURCE` recibe alimentación desde conceptos origen.

#### Ejemplos
- `BASE_CC` depende de `SALARIO_BASE`
- `BASE_IRPF` depende de `PRORRATA_EXTRA`

#### Origen
ADR-038 — Estrategias de agregación

#### Naturaleza
- derivada de relaciones de alimentación (`ConceptFeedRelation`)
- definida en el concepto origen

---

### 3. `SELECTION_DEPENDENCY`

#### Definición
Se produce cuando un `AGGREGATE` con estrategia `SELECT_BY_RULE` depende de los conceptos que cumplen su regla de selección.

#### Ejemplos
- `TOTAL_DEVENGOS` depende de todos los conceptos con `functionalNature = EARNING`
- `TOTAL_DEDUCCIONES` depende de todos los conceptos con `functionalNature = DEDUCTION`

#### Origen
ADR-038 — Estrategias de agregación

#### Naturaleza
- derivada
- dependiente del catálogo de conceptos y del contexto de evaluación

---

## Grafo de cálculo

### Definición

El conjunto de conceptos y sus dependencias forma un **grafo dirigido de cálculo** donde:

- los nodos representan `PayrollConcept`
- las aristas representan dependencias

---

### Interpretación

Una arista:


B → A


significa:

> El concepto B debe ser evaluado antes que el concepto A.

---

## Propiedades del grafo

---

### 1. Aciclicidad

El grafo debe ser un **grafo dirigido acíclico (DAG)**.

#### Consecuencia
No se permiten ciclos de dependencias entre conceptos.

#### Ejemplo inválido
- `BASE_CC` depende de `TOTAL_DEVENGOS`
- `TOTAL_DEVENGOS` depende de `SALARIO_BASE`
- `SALARIO_BASE` depende de `BASE_CC`

---

### 2. Dependencias explícitas o derivables

Toda dependencia debe ser:

- explícita (operandos con `source = CONCEPT`)
- o derivable (feeds o reglas de selección)

#### Regla
No se permiten dependencias implícitas o no declaradas.

---

### 3. Completitud estructural

El sistema debe ser capaz de:

- construir el conjunto completo de dependencias
- a partir de la configuración del modelo

antes de cualquier ejecución.

---

### 4. Independencia del contexto de ejecución

El modelo de dependencias es una propiedad estructural del sistema y:

- no depende de un empleado concreto
- no depende de una nómina concreta

---

## Grafo configurado y grafo efectivo

Se distinguen dos niveles de representación.

---

### Grafo configurado

Representa:

- todas las dependencias potenciales derivadas del metamodelo

Uso:

- validación estructural
- detección de ciclos
- análisis de impacto
- tooling

---

### Grafo efectivo

Representa:

- las dependencias realmente activas en una ejecución concreta

Uso:

- ejecución del cálculo
- trazabilidad
- debugging

---

## Construcción del grafo

El grafo se construye a partir de:

1. dependencias por operandos (`source = CONCEPT`)
2. relaciones de alimentación (`FEED_BY_SOURCE`)
3. reglas de selección (`SELECT_BY_RULE`)

---

## Consecuencias

---

### Positivas

- orden de cálculo derivable automáticamente  
- detección temprana de ciclos  
- trazabilidad completa del cálculo  
- base para ejecución declarativa  
- desacoplamiento entre conceptos  

---

### Costes

- mayor complejidad conceptual  
- necesidad de validación estructural  
- necesidad de herramientas de inspección del grafo  

---

## Riesgos identificados

---

### 1. Ciclos indirectos

Dependencias encadenadas pueden generar ciclos no triviales.

---

### 2. Dependencias mal definidas

Errores en configuración pueden generar dependencias inexistentes o incoherentes.

---

### 3. Selección dinámica no controlada

`SELECT_BY_RULE` debe mantenerse dentro de un conjunto acotado de reglas para evitar comportamientos impredecibles.

---

## No objetivos

Este ADR no define:

- estrategia de ejecución del grafo  
- orden de evaluación concreto  
- paralelización  
- caching  
- segmentación temporal  
- activación contextual de conceptos  

---

## Resumen ejecutivo

Se establece que los `PayrollConcept` forman un grafo dirigido de dependencias donde:

- los nodos representan conceptos  
- las aristas representan relaciones de dependencia  

Se definen tres tipos de dependencia:

- `OPERAND_DEPENDENCY`
- `FEED_DEPENDENCY`
- `SELECTION_DEPENDENCY`

El grafo debe ser acíclico, explícito y completamente derivable de la configuración.

Este modelo constituye la base para la futura ejecución del motor de cálculo.