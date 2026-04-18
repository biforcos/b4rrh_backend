Vamos a por el siguiente bloque clave. Este ADR es el que convierte las tipologías en motor real configurable.

# ADR-037 — Sources y resolución de operandos en `PayrollConcept`

## Estado
Propuesto

---

## Contexto

El ADR-036 define las tipologías canónicas de cálculo de `PayrollConcept`:

- `DIRECT_AMOUNT`
- `RATE_BY_QUANTITY`
- `PERCENTAGE`
- `AGGREGATE`

Estas tipologías describen únicamente la **forma del cálculo**, pero no especifican:

- de dónde provienen los valores necesarios;
- cómo se resuelven los operandos en tiempo de ejecución.

Sin una capa explícita de resolución de operandos:

- el sistema tendería a introducir lógica específica por concepto;
- se perdería configurabilidad;
- se dificultaría la reutilización;
- aumentaría el acoplamiento entre cálculo y origen de datos.

Por tanto, es necesario definir un modelo claro de **sources de operandos** que permita desacoplar completamente:

- la operación (tipo de cálculo)
- el origen de los datos

---

## Decisión

Se introduce el concepto de **source de operando**, que define el origen del valor utilizado en un cálculo.

Cada operando de un `PayrollConcept` se resuelve mediante:

- un `sourceType`
- una referencia asociada (según el tipo)

---

## Principio rector

Un operando no contiene un valor directo, sino una **instrucción de resolución**.

---

## Sources canónicos iniciales

Se definen los siguientes tipos de source:

- `INPUT`
- `CONSTANT`
- `TABLE`
- `CONCEPT`
- `EMPLOYEE_DATA`
- `PERIOD_DATA`
- `SEGMENT_DATA`

---

## Definición de cada source

### 1. `INPUT`

#### Descripción
Valor informado externamente para el cálculo.

#### Ejemplos
- horas extra introducidas
- unidades manuales
- importes excepcionales

#### Uso típico
- `quantity`
- `amount`

---

### 2. `CONSTANT`

#### Descripción
Valor fijo parametrizado en el sistema.

#### Ejemplos
- importe fijo mensual
- porcentaje fijo
- divisor estándar (ej. 30)

#### Uso típico
- `rate`
- `percentage`
- `amount`

---

### 3. `TABLE`

#### Descripción
Valor obtenido a partir de una tabla parametrizada.

#### Ejemplos
- salario por categoría
- tarifa por hora
- porcentaje por tramo

#### Uso típico
- `rate`
- `percentage`
- `amount`

---

### 4. `CONCEPT`

#### Descripción
Valor obtenido a partir del resultado de otro `PayrollConcept`.

#### Ejemplos
- `BASE_CC`
- `BASE_IRPF`
- `DIAS_PRESENCIA`
- `PRECIO_DIA`

#### Uso típico
- cualquier operando

#### Nota clave
Este source habilita la **composición del motor**, permitiendo construir cadenas de cálculo reutilizables.

---

### 5. `EMPLOYEE_DATA`

#### Descripción
Dato estructural del empleado.

#### Ejemplos
- porcentaje de jornada
- categoría profesional
- tipo de contrato

#### Uso típico
- inputs para tablas
- cálculo de valores derivados

---

### 6. `PERIOD_DATA`

#### Descripción
Dato asociado al período completo de cálculo.

#### Ejemplos
- días del mes
- año/mes
- número de pagas

#### Uso típico
- `quantity`
- cálculos base

---

### 7. `SEGMENT_DATA`

#### Descripción
Dato asociado a un tramo homogéneo de cálculo dentro del período.

#### Ejemplos
- días del segmento
- jornada vigente en el segmento
- condiciones activas en el tramo

#### Uso típico
- `quantity`
- cálculos intraperiodo

---

## Resolución de operandos por tipo de cálculo

---

### `DIRECT_AMOUNT`

#### Operando
- `amount`

#### Sources permitidos
- `INPUT`
- `CONSTANT`
- `TABLE`
- `CONCEPT`

---

### `RATE_BY_QUANTITY`

#### Operandos
- `quantity`
- `rate`

#### `quantitySource`
- `INPUT`
- `CONCEPT`
- `PERIOD_DATA`
- `SEGMENT_DATA`

#### `rateSource`
- `CONSTANT`
- `TABLE`
- `CONCEPT`

---

### `PERCENTAGE`

#### Operandos
- `base`
- `percentage`

#### `baseSource`
- `CONCEPT`
- `PERIOD_DATA`
- `SEGMENT_DATA`

#### `percentageSource`
- `CONSTANT`
- `TABLE`
- `CONCEPT`

---

### `AGGREGATE`

#### Operando
- `membership`

#### Nota
La resolución de miembros no se modela como source, sino mediante estrategias de agregación definidas en ADR posterior.

---

## Regla fundamental

Los tipos de cálculo **no contienen lógica de negocio específica**, sino que delegan completamente la obtención de valores en los sources.

---

## Composicionalidad del motor

El uso de `CONCEPT` como source permite:

- construir conceptos técnicos reutilizables;
- encadenar cálculos;
- separar lógica compleja en piezas simples;
- mejorar trazabilidad y debugging.

---

## Ejemplo conceptual

### Salario base


quantity = CONCEPT(DIAS_PRESENCIA)
rate = CONCEPT(PRECIO_DIA)
resultado = quantity × rate


---

### IRPF


base = CONCEPT(BASE_IRPF)
percentage = CONCEPT(TIPO_IRPF_EFECTIVO)
resultado = base × percentage


---

## Consecuencias

### Positivas

- desacoplamiento total entre cálculo y origen de datos;
- alta configurabilidad;
- reutilización de lógica;
- facilidad para introducir conceptos técnicos;
- base para motor declarativo.

---

### Costes

- necesidad de definir correctamente catálogo de conceptos técnicos;
- mayor complejidad conceptual inicial;
- necesidad de validaciones fuertes entre tipos y sources.

---

## No objetivos

Este ADR no define:

- modelo físico de persistencia de sources;
- resolución concreta de tablas;
- implementación de motor de cálculo;
- orden de ejecución de conceptos;
- versionado de parámetros.

---

## Resumen ejecutivo

Se define un modelo de resolución de operandos basado en sources tipados.

Cada operando de un concepto se resuelve mediante un source, desacoplando completamente:

- el tipo de cálculo
- el origen de los datos

Este modelo permite construir un motor composicional, reutilizable y altamente con