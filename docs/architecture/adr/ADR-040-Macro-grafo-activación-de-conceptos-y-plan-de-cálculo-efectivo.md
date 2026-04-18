# ADR-040 — Macro-grafo, activación de conceptos y plan de cálculo efectivo

## Estado
Propuesto

---

## Contexto

El ADR-039 define que los `PayrollConcept` forman un grafo dirigido acíclico (DAG) basado en dependencias:

- `OPERAND_DEPENDENCY`
- `FEED_DEPENDENCY`
- `SELECTION_DEPENDENCY`

Sin embargo, este grafo:

- representa **todas las dependencias posibles**
- no distingue qué conceptos deben calcularse en una ejecución concreta

Para poder ejecutar el motor, es necesario definir:

1. cómo se determina qué conceptos participan
2. cómo se reduce el grafo al subconjunto relevante
3. cómo se obtiene un orden de cálculo válido

---

## Decisión

Se introduce un modelo de **macro-grafo + activación + plan efectivo** en tres fases:

1. **Macro-grafo configurado**
2. **Activación de conceptos**
3. **Plan de cálculo efectivo**

---

## 1. Macro-grafo configurado

### Definición

El macro-grafo es:

> el grafo completo de todos los `PayrollConcept` y sus dependencias estructurales

Incluye:

- todos los conceptos definidos en el sistema
- todas las dependencias posibles derivadas del modelo

---

### Propiedades

- es global al `ruleSystem`
- es independiente de empleado o periodo
- es estático salvo cambios de configuración
- es validado estructuralmente (ciclos, coherencia)

---

### Uso

- validación del sistema
- análisis de impacto
- tooling (visualización, debugging)
- base para generación de planes efectivos

---

## 2. Activación de conceptos

### Definición

La activación determina:

> qué conceptos deben calcularse en una ejecución concreta

---

### Tipos de activación

Se definen tres mecanismos canónicos:

---

#### 2.1 Activación explícita (`EXPLICIT`)

Conceptos solicitados directamente por el sistema.

#### Ejemplos

- cálculo de `NETO_A_PERCIBIR`
- cálculo de `TOTAL_DEVENGOS`

---

#### 2.2 Activación por dependencia (`DEPENDENCY`)

Se activan todos los conceptos necesarios para calcular los conceptos explícitos.

#### Regla

Si A está activado y A depende de B, entonces B se activa.

---

#### 2.3 Activación por selección (`SELECTION`)

Se activan conceptos seleccionados dinámicamente por reglas de agregación.

#### Ejemplos

- todos los conceptos con `functionalNature = EARNING`
- todos los conceptos marcados como cotizables

---

### Resultado de la activación

Se obtiene:

> un subconjunto de nodos del macro-grafo llamado **conjunto activo de conceptos**

---

## 3. Subgrafo efectivo

### Definición

El subgrafo efectivo es:

> el grafo inducido por el conjunto activo de conceptos

Incluye:

- todos los nodos activados
- todas las dependencias entre ellos

---

### Propiedades

- es un subgrafo del macro-grafo
- sigue siendo acíclico
- es específico de una ejecución

---

## 4. Plan de cálculo efectivo

### Definición

El plan de cálculo es:

> una ordenación válida de los conceptos activos que respeta todas las dependencias

---

### Construcción

Se obtiene mediante una **ordenación topológica** del subgrafo efectivo.

---

### Propiedades

- todo concepto se evalúa después de sus dependencias
- no existe ambigüedad en el orden relativo necesario
- puede existir más de un orden válido

---

### Representación

El plan puede representarse como:

- lista ordenada de conceptos
- niveles de cálculo (capas paralelizables)
- pipeline de ejecución

---

## Ejemplo conceptual

Dado el objetivo:


NETO_A_PERCIBIR


### Activación

Se activan:

- NETO_A_PERCIBIR
- TOTAL_DEVENGOS
- TOTAL_DEDUCCIONES
- SALARIO_BASE
- IRPF
- BASE_IRPF
- ...
Subgrafo efectivo

Se construye el grafo con esos nodos y sus dependencias.

Plan resultante (ejemplo)
DIAS_PRESENCIA
PRECIO_DIA
SALARIO_BASE
BASE_IRPF
TIPO_IRPF_EFECTIVO
IRPF
TOTAL_DEVENGOS
TOTAL_DEDUCCIONES
NETO_A_PERCIBIR
Separación de responsabilidades

Este ADR establece una separación clara:

Fase	Responsabilidad
Macro-grafo	modelo estructural
Activación	qué calcular
Subgrafo	reducción del problema
Plan	cómo ordenarlo
Consecuencias
Positivas
ejecución derivada automáticamente
desacoplamiento total entre definición y ejecución
capacidad de calcular subconjuntos
base para paralelización futura
trazabilidad clara
Costes
necesidad de construir subgrafos dinámicos
necesidad de resolver activación correctamente
mayor complejidad conceptual
Riesgos
1. Activación incompleta

Si falta un concepto necesario → fallo en ejecución.

2. Activación excesiva

Activar conceptos innecesarios → coste de cálculo innecesario.

3. Reglas de selección mal definidas

Pueden activar conjuntos inesperados de conceptos.

No objetivos

Este ADR no define:

cómo se calcula cada concepto
cómo se gestionan segmentos temporales
cómo se cachean resultados
cómo se ejecuta en paralelo
cómo se materializan resultados
Relación con ADRs previos
ADR-036 → define tipos de cálculo
ADR-037 → define sources y operandos
ADR-038 → define agregación
ADR-039 → define dependencias

Este ADR define:

cómo todo lo anterior se convierte en un plan ejecutable

Resumen ejecutivo

El sistema se modela como:

un macro-grafo completo de conceptos
un proceso de activación que determina qué calcular
un subgrafo efectivo reducido
un plan de cálculo derivado por ordenación topológica

Este enfoque permite ejecutar el motor de forma declarativa, predecible y extensible.