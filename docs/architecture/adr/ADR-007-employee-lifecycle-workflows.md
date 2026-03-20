# ADR — Employee Lifecycle Workflows (Hire / Terminate / Rehire)

## Status

Proposed

## Context

El sistema B4RRHH ha sido diseñado siguiendo una arquitectura basada en verticales funcionales independientes (contacts, addresses, identifiers, presence, etc.), todas ellas relacionadas con el empleado mediante business key.

Este enfoque ha permitido:

* Separación clara de responsabilidades
* Evolución independiente de cada vertical
* APIs limpias y desacopladas
* Integración progresiva en frontend mediante composición de bloques

Sin embargo, este modelo presenta una limitación desde el punto de vista funcional:

> El ciclo de vida del empleado no se corresponde con la creación y mantenimiento manual de múltiples verticales independientes.

En la práctica, acciones como contratar, despedir o recontratar a un empleado implican:

* creación o modificación coordinada de múltiples verticales
* reglas de coherencia temporal (fechas efectivas)
* validaciones transversales
* significado funcional único (no técnico)

Actualmente, el sistema permitiría modelar estas acciones como una secuencia de operaciones independientes (crear employee, luego presence, luego assignment, etc.), lo cual:

* no refleja el dominio real
* degrada la experiencia de usuario
* aumenta el riesgo de inconsistencias

Por tanto, se identifica la necesidad de introducir una nueva capa de **operaciones de negocio compuestas**, que representen el ciclo de vida del empleado.

---

## Decision

Se introduce el concepto de **Employee Lifecycle Workflows**, como una capa funcional por encima de las verticales existentes.

Estos workflows representan acciones de negocio completas que afectan a múltiples partes del modelo de empleado de forma coordinada.

### Workflows iniciales definidos

* **Hire Employee**
* **Terminate Employee**
* **Rehire Employee**

Estos workflows:

* no sustituyen a las verticales existentes
* no alteran el modelo de datos base
* actúan como orquestadores de operaciones sobre múltiples verticales

---

## Design Principles

### 1. Separación entre recursos y acciones

Se distingue claramente entre:

* **Recursos de dominio**
  (employee, presence, contact, identifier, etc.)

* **Acciones de negocio**
  (hire, terminate, rehire)

Los workflows no son recursos persistentes, sino casos de uso.

---

### 2. Orquestación coherente

Cada workflow:

* ejecuta múltiples operaciones sobre distintas verticales
* garantiza consistencia funcional (fechas, estados, relaciones)
* evita que el usuario tenga que ensamblar manualmente el estado del empleado

---

### 3. Persistencia desacoplada

Las verticales existentes:

* mantienen su diseño actual
* siguen siendo accesibles de forma independiente
* siguen siendo la base del modelo

Los workflows no introducen nuevas tablas “monolíticas”.

---

### 4. UX orientada a intención

El sistema debe permitir que el usuario piense en términos de:

* “contratar empleado”
* “despedir empleado”

y no en:

* “crear presence”
* “crear assignment”
* “actualizar estado”

---

### 5. Transparencia

Los workflows deben:

* ser explícitos en qué operaciones realizan
* evitar efectos ocultos
* permitir trazabilidad futura

---

## Workflow Definitions

### 1. Hire Employee

#### Descripción

Inicia la vida laboral de un empleado en el sistema.

#### Operaciones implicadas

* creación de employee core
* creación de primera presence
* creación de asignación organizativa inicial (work center, cost center, etc.)
* inicialización de estado laboral

#### Datos mínimos esperados (orientativo)

* employeeNumber
* employeeTypeCode
* ruleSystemCode
* nombre y apellidos
* fecha de entrada
* entryReasonCode
* companyCode
* workCenter (u otra asignación organizativa mínima)

#### Reglas clave

* todas las entidades iniciales deben compartir coherencia temporal
* debe existir una presence activa tras el proceso
* el empleado queda en estado funcional válido

---

### 2. Terminate Employee

#### Descripción

Finaliza la relación laboral de un empleado.

#### Operaciones implicadas

* cierre de presence activa
* registro de fecha de salida
* registro de exitReasonCode
* cierre o ajuste de asignaciones vigentes

#### Reglas clave

* no puede existir más de una presence activa
* tras la terminación no debe quedar ninguna presence abierta
* se preserva el histórico completo

---

### 3. Rehire Employee

#### Descripción

Reincorpora a un empleado previamente terminado.

#### Operaciones implicadas

* creación de nueva presence
* creación de nuevas asignaciones iniciales
* reutilización del employee existente

#### Reglas clave

* no se crea un nuevo employee
* se mantiene histórico de presencias anteriores
* la nueva presence debe ser coherente con las anteriores

---

## API Considerations (Future)

Se prevé la introducción de endpoints específicos para workflows, por ejemplo:

* `POST /employees/hire`
* `POST /employees/{employeeId}/terminate`
* `POST /employees/{employeeId}/rehire`

Estos endpoints:

* encapsularán la lógica de orquestación
* recibirán payloads orientados a negocio
* no expondrán directamente detalles internos de cada vertical

---

## UI Considerations (Future)

Los workflows se expondrán como acciones de primer nivel en la navegación:

* Employee

  * Ficha (visualización y mantenimiento)
  * Contratar (Hire)
  * Despedir (Terminate)
  * Recontratar (Rehire)

Cada workflow se implementará como:

* pantalla dedicada o flujo guiado (no modal simple)
* formulario estructurado por bloques
* validación previa antes de ejecución

---

## Consequences

### Positivas

* Mejor alineación con el dominio real
* Mejora significativa de UX
* Reducción de inconsistencias funcionales
* Reutilización del modelo existente
* Escalabilidad para nuevas acciones de negocio

### Negativas / Riesgos

* Incremento de complejidad en capa de aplicación
* Necesidad de definir reglas de negocio claras
* Posible duplicidad si no se gobierna bien la relación entre workflows y verticales

---

## Alternatives Considered

### 1. Mantener solo operaciones CRUD por vertical

Descartado:

* no representa el dominio real
* UX pobre
* alto riesgo de inconsistencias

### 2. Convertir employee en un agregado monolítico

Descartado:

* rompe la arquitectura modular actual
* reduce flexibilidad
* dificulta evolución

---

## Open Questions

* Definición exacta del mínimo necesario para cada workflow
* Gestión de validaciones complejas entre verticales
* Estrategia de versionado de workflows
* Auditoría y trazabilidad de ejecuciones

---

## Summary

El empleado no debe modelarse únicamente como un conjunto de datos, sino como un objeto con ciclo de vida.

La introducción de **Employee Lifecycle Workflows** permite:

* mantener la arquitectura modular existente
* añadir una capa funcional coherente con el negocio
* mejorar significativamente la experiencia de usuario

Este ADR establece la base conceptual para futuras implementaciones de Hire, Terminate y Rehire en el sistema B4RRHH.
