# ADR-041 — Segmentación temporal, ámbito de ejecución y cálculo por tramos en `PayrollConcept`

## Estado
Propuesto

---

## Contexto

Los ADR previos establecen:

- ADR-036 — Tipologías de cálculo de `PayrollConcept`
- ADR-037 — Resolución de operandos mediante sources
- ADR-038 — Estrategias de agregación (`FEED_BY_SOURCE`, `SELECT_BY_RULE`)
- ADR-039 — Modelo de dependencias y grafo de cálculo (DAG)
- ADR-040 — Macro-grafo, activación y plan efectivo

Estos elementos permiten definir qué calcular y en qué orden.

Sin embargo, en nómina real, durante un mismo período pueden producirse cambios que afectan al cálculo:

- jornada laboral  
- salario  
- contrato  
- centro de trabajo  
- situaciones de alta/baja  
- otras condiciones relevantes  

Esto implica que el cálculo no puede realizarse como una única ejecución homogénea.

---

## Decisión

Se introduce un modelo de:

1. **segmentación temporal del período**
2. **ejecución del plan de cálculo por segmento**
3. **clasificación de conceptos por ámbito temporal (`executionScope`)**
4. **consolidación de resultados a nivel de período**

---

## 1. Modelo temporal

### 1.1 `CalculationPeriod`

Representa el período global de la nómina:

- `periodStart`
- `periodEnd`

---

### 1.2 `CalculationSegment`

Representa un subtramo homogéneo dentro del período:

- `segmentStart`
- `segmentEnd`

---

### Propiedad clave

Dentro de un segmento:

> Las condiciones relevantes para el cálculo permanecen constantes.

---

## 2. Segmentación

### Definición

El período se divide en:

> un conjunto ordenado de segmentos contiguos, no solapados y exhaustivos

---

### Propiedades

- cubren completamente el período  
- no se solapan  
- son deterministas  
- son reproducibles  

---

### Origen de los cortes

Los segmentos se generan por cambios en condiciones relevantes del cálculo:

- datos del empleado  
- asignaciones  
- condiciones contractuales  
- otros factores que afectan al cálculo  

---

### Regla importante

Un segmento puede estar delimitado por múltiples cambios simultáneos.

#### Consecuencia

No se modela una única “fuente del segmento”.

---

## 3. Contexto de ejecución por segmento

Cada ejecución del cálculo se realiza con un contexto temporal enriquecido:

- `periodStart`
- `periodEnd`
- `segmentStart`
- `segmentEnd`
- `isFirstSegment`
- `isLastSegment`

---

## 4. Relación con el grafo de cálculo

### Regla fundamental

> La segmentación no modifica la topología del grafo de cálculo.

---

### Implicación

- el macro-grafo y el plan efectivo son únicos  
- se reutilizan para todos los segmentos  

---

### Ejecución

El plan de cálculo:

> se ejecuta una vez por cada segmento con distinto contexto temporal

---

## 5. Ámbito de ejecución del concepto

Se introduce la propiedad:

# `executionScope`

---

### Definición

Define el nivel temporal en el que se evalúa un concepto.

---

### Valores iniciales

- `SEGMENT`
- `PERIOD`

---

### Regla fuerte

> `executionScope` es una propiedad inmutable del concepto.

#### Consecuencia

Cambiar el ámbito implica crear un nuevo concepto.

---

### Interpretación

#### `SEGMENT`

El concepto se evalúa en cada segmento.

Ejemplos:

- salario base  
- horas trabajadas  
- pluses proporcionales  

---

#### `PERIOD`

El concepto se evalúa una única vez para todo el período.

Ejemplos:

- totales  
- agregados finales  
- ciertos cálculos acumulados  

---

## 6. Ejecución segmentada

### Proceso

1. Se construyen los segmentos del período  
2. Se ejecuta el plan de cálculo para cada segmento (`executionScope = SEGMENT`)  
3. Se obtienen resultados parciales  
4. Se consolidan los resultados a nivel de período  
5. Se evalúan conceptos de `executionScope = PERIOD`

---

## 7. Consolidación

### Definición

Proceso de agregación de resultados de segmentos.

---

### Ejemplos

- suma de importes segmentados  
- construcción de bases  
- preparación de datos para conceptos de período  

---

### Nota

La consolidación es un paso previo a la evaluación de conceptos de ámbito `PERIOD`.

---

## 8. Trazabilidad y reproducibilidad

### Regla clave

> La segmentación utilizada en un cálculo debe ser determinista, reproducible y auditable.

---

### Decisión

Los segmentos forman parte del:

> **snapshot técnico del cálculo de nómina**

---

### Consecuencia

Es posible:

- reconstruir cómo se calculó la nómina  
- explicar los tramos utilizados  
- garantizar coherencia en retroactividad  

---

## 9. Validaciones

---

### 9.1 Validación de segmentación

Debe garantizar:

- cobertura completa del período  
- ausencia de solapamientos  
- orden correcto  

---

### 9.2 Validación de ejecución

Debe garantizar:

- coherencia entre `executionScope` y uso del concepto  
- disponibilidad de datos necesarios en cada segmento  
- correcta consolidación  

---

## 10. Riesgos identificados

---

### 10.1 Segmentación no determinista

Provoca inconsistencias en recalculaciones.

---

### 10.2 Uso incorrecto de `executionScope`

Puede generar:

- doble cálculo  
- omisiones  
- incoherencias  

---

### 10.3 Mala clasificación de conceptos

Asignar incorrectamente `SEGMENT` o `PERIOD` rompe la lógica del cálculo.

---

### 10.4 Explicación simplificada de cortes

Asociar un único motivo a un segmento puede ser incorrecto.

---

## 11. No objetivos

Este ADR no define:

- algoritmo de generación de segmentos  
- optimización de ejecución  
- paralelización  
- caching  
- persistencia detallada de estructuras internas  

---

## 12. Insight clave

El cálculo de nómina evoluciona de:

> una ejecución única del grafo

a:

> la ejecución del mismo plan de cálculo sobre múltiples contextos temporales homogéneos, seguida de una consolidación

---

## 13. Conclusión

Se establece que:

- el período se segmenta en tramos homogéneos  
- el mismo plan de cálculo se ejecuta por segmento  
- los conceptos se clasifican por ámbito temporal (`executionScope`)  
- los resultados se consolidan a nivel de período  
- la segmentación es determinista y trazable  

Este modelo permite:

- soportar cambios intraperiodo  
- mantener coherencia en retroactividad  
- preservar un único grafo de cálculo  
- garantizar trazabilidad completa del resultado  