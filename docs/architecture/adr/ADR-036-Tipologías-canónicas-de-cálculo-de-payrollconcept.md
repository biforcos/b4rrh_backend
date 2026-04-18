# ADR-036 — Tipologías canónicas de cálculo de `PayrollConcept`

## Estado
Propuesto

---

## Contexto

El proyecto B4RRHH define un motor de nómina basado en un metamodelo de objetos (`PayrollObject`), donde los conceptos de nómina (`PayrollConcept`) representan unidades funcionales de cálculo dentro de una nómina.

Una de las decisiones clave del motor es evitar implementar lógica específica por concepto mediante código, y en su lugar permitir que los conceptos se configuren a partir de un conjunto limitado de tipologías de cálculo y reglas de composición.

Sin una tipología clara:

- el sistema tendería a crecer mediante lógica específica por concepto;
- se perdería la capacidad de configuración;
- aumentaría la deuda técnica;
- se dificultaría la trazabilidad y la retroactividad.

Por tanto, es necesario definir un conjunto reducido, estable y expresivo de **tipos de cálculo canónicos** que cubran la mayoría de casos reales sin inflar el modelo.

---

## Decisión

Se definen las siguientes tipologías canónicas de cálculo para `PayrollConcept`:

- `DIRECT_AMOUNT`
- `RATE_BY_QUANTITY`
- `PERCENTAGE`
- `AGGREGATE`

Cada tipo representa una **forma fundamental de cálculo**, no un caso de negocio concreto.

---

## Principio rector

El tipo de cálculo describe el **operador principal** del concepto.

No describe:
- el origen de los datos;
- la semántica concreta del concepto (ej. “salario base”, “IRPF”);
- ni la forma específica en que se obtienen sus operandos.

---

## Tipologías definidas

### 1. `DIRECT_AMOUNT`

#### Definición
El resultado del concepto es un importe directo ya resuelto.

#### Forma general

resultado = amount


#### Características
- No depende de otros operandos estructurados.
- Representa un valor final ya calculado o informado.

#### Ejemplos
- ajuste manual
- plus fijo mensual
- cuantía fija por tabla
- regularización directa

---

### 2. `RATE_BY_QUANTITY`

#### Definición
El resultado del concepto se obtiene como el producto de una cantidad por un precio.

#### Forma general

resultado = quantity × rate


#### Características
- Generaliza múltiples casos de negocio:
  - días × precio día
  - horas × precio hora
  - unidades × tarifa
- No define cómo se obtienen `quantity` ni `rate`.

#### Ejemplos
- salario base diario
- horas extra
- plus por día trabajado
- dietas
- kilometraje

#### Nota importante
Conceptos tradicionalmente considerados como “basados en presencia” se modelan como casos particulares de este tipo, donde la cantidad representa días computables.

---

### 3. `PERCENTAGE`

#### Definición
El resultado del concepto se obtiene aplicando un porcentaje sobre una base.

#### Forma general

resultado = base × percentage


#### Características
- Separa claramente la base del porcentaje.
- No define cómo se obtiene el porcentaje.

#### Ejemplos
- cotización a la seguridad social
- IRPF
- complementos porcentuales

#### Nota importante
Incluso cuando el porcentaje se obtiene mediante lógica compleja (ej. IRPF), el concepto sigue perteneciendo a esta tipología.  
La complejidad se desplaza a la obtención del porcentaje, no al tipo de cálculo.

---

### 4. `AGGREGATE`

#### Definición
El resultado del concepto se obtiene combinando resultados de otros conceptos ya calculados.

#### Forma general

resultado = SUM(miembros)


#### Características
- No opera sobre datos primarios, sino sobre resultados previos.
- Representa composición o acumulación.

#### Ejemplos
- bases de cotización
- bases fiscales
- total devengos
- total deducciones

#### Nota importante
La forma de determinar los miembros no forma parte de la tipología, sino de una capa adicional (definida en ADR posterior).

---

## Reglas de diseño

### 1. Minimalismo tipológico

No se crearán nuevos tipos de cálculo por cada caso de negocio frecuente.

Ejemplo descartado:
- `PRESENCE_BASED`

Motivo:
- no representa una operación distinta;
- describe una forma de obtener un operando (`quantity`), no un tipo de cálculo.

---

### 2. Separación de responsabilidades

Se separan claramente:

- tipo de cálculo → define la operación
- resolución de operandos → define de dónde salen los datos

Esta separación es fundamental para:

- evitar explosión de tipos;
- permitir configuración;
- facilitar reutilización.

---

### 3. Composicionalidad

Los tipos de cálculo deben permitir que los operandos provengan de resultados de otros conceptos.

Esto habilita:

- conceptos técnicos intermedios;
- cadenas de cálculo reutilizables;
- construcción incremental del resultado de nómina.

---

### 4. Inmutabilidad del tipo

El `calculationType` de un `PayrollConcept` es inmutable.

#### Consecuencia
Si un concepto cambia su naturaleza de cálculo:
- no se versiona;
- se crea un nuevo concepto.

#### Motivación
- preservar coherencia histórica;
- evitar ambigüedad semántica;
- simplificar retroactividad.

---

## Consecuencias

### Positivas

- modelo estable y predecible;
- reducción drástica de lógica específica por concepto;
- alta capacidad de configuración;
- base sólida para evolución del motor;
- alineación con arquitectura hexagonal y metamodelo del proyecto.

---

### Costes

- necesidad de modelar correctamente operandos y sources;
- mayor esfuerzo inicial de diseño;
- algunos casos complejos requerirán conceptos técnicos adicionales en lugar de lógica directa.

---

## No objetivos

Este ADR no define:

- cómo se resuelven los operandos (`sources`);
- cómo se versionan las reglas;
- el orden de ejecución de los conceptos;
- el modelo de persistencia;
- la API de configuración;
- la estrategia de agregación de `AGGREGATE`.

---

## Resumen ejecutivo

Se establece un conjunto mínimo y completo de tipologías de cálculo para `PayrollConcept`:

- `DIRECT_AMOUNT`
- `RATE_BY_QUANTITY`
- `PERCENTAGE`
- `AGGREGATE`

Estas tipologías representan las formas fundamentales de cálculo del motor y permiten modelar la mayoría de los conceptos de nómina mediante configuración, sin necesidad de lógica específica por concepto.

El modelo se apoya en la separación entre:

- operación (tipo de cálculo)
- resolución de datos (operandos)

lo que habilita un motor flexible, composicional y extensible.