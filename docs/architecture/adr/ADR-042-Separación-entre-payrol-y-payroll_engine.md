# ADR-042 — Separación entre `payroll` y `payroll_engine`

## Estado
Propuesto

---

## Contexto

El diseño del motor de nómina de B4RRHH ha evolucionado desde un enfoque potencialmente basado en lógica específica por concepto hacia un modelo configurable basado en:

- `PayrollObject`
- `PayrollConcept`
- tipologías de cálculo
- sources y operandos
- estrategias de agregación
- grafo de dependencias
- segmentación temporal

En paralelo, el bounded context `payroll` ya existe para modelar:

- nóminas calculadas
- estados de nómina
- runs de cálculo
- claims
- mensajes
- snapshots del cálculo

A medida que madura el metamodelo del motor, aparece una separación semántica clara entre:

1. **la definición de cómo se calcula una nómina**
2. **la persistencia del resultado de una nómina calculada**

Mezclar ambas naturalezas en el mismo schema o subdominio introduce ambigüedad de diseño.

---

## Decisión

Se separan explícitamente dos ámbitos:

- `payroll`
- `payroll_engine`

---

## 1. Ámbito `payroll`

`payroll` modela la nómina calculada como resultado de negocio.

Incluye, entre otros:

- payroll root
- líneas calculadas
- estados
- calculation runs
- claims
- mensajes
- snapshots técnicos del cálculo
- segmentos utilizados en una ejecución concreta

### Naturaleza
Resultado materializado del cálculo.

---

## 2. Ámbito `payroll_engine`

`payroll_engine` modela el metamodelo y la configuración técnico-funcional del motor.

Incluye, entre otros:

- `PayrollObject`
- `PayrollConcept`
- feeds entre conceptos
- tablas
- constantes
- tipologías de cálculo
- scopes de ejecución
- metadatos de resolución

### Naturaleza
Definición estructural de cómo se calcula una nómina.

---

## Regla principal

> La configuración y metamodelo del motor de nómina no deben persistirse en el mismo ámbito semántico que las nóminas calculadas.

---

## Consecuencias

### Positivas

- separación clara entre configuración y resultado
- mejor trazabilidad
- mejor capacidad de gobierno del motor
- menor contaminación semántica del bounded context `payroll`
- base más limpia para evolución futura

### Costes

- aparece un nuevo ámbito de diseño
- exige modelar explícitamente la relación entre runtime del motor y resultado calculado

---

## Regla operativa

Los artefactos persistentes que definan **cómo se calcula** una nómina pertenecen a `payroll_engine`.

Los artefactos persistentes que representen **una nómina ya calculada** pertenecen a `payroll`.

---

## No objetivos

Este ADR no define todavía:

- estructura física detallada de schemas
- APIs de mantenimiento del motor
- estrategia de despliegue
- separación en repositorios o servicios

---

## Resumen ejecutivo

Se establece una frontera explícita:

- `payroll` = resultado calculado
- `payroll_engine` = definición del motor

Esto evita mezclar metamodelo y cálculo materializado en el mismo dominio y prepara una base más limpia para la implementación.