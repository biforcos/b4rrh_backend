# ADR-036 — Tipologías canónicas de cálculo de `PayrollConcept`

## Estado
Aceptado

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
El resultado del concepto se obtiene combinando resultados de otros conceptos ya calculados, pudiendo invertir el signo de cada contribuyente individualmente.

#### Forma general

resultado = SUM(feed_i × sign_i)

donde `sign_i` es +1 si la relación de feed tiene `invert_sign = false`, y −1 si tiene `invert_sign = true`.

#### El flag `invert_sign` en la relación de feed

El flag `invert_sign` reside en la **relación de feed** (no en el concepto fuente). Esto permite que un mismo concepto contribuya positivamente a un agregado y negativamente a otro:

| Concepto fuente       | Feeds aggregate | `invert_sign` | Efecto         |
|-----------------------|-----------------|---------------|----------------|
| 101 SALARIO_BASE      | 970             | false         | + importe      |
| 101 SALARIO_BASE      | 990             | false         | + importe      |
| concepto deducción X  | 980             | false         | + importe      |
| concepto deducción X  | 990             | true          | − importe      |

#### Modelo de grafo plano (flat graph)

Los conceptos hoja alimentan **directamente** tanto su agregado lateral (devengos o deducciones) como el agregado de líquido neto (990). El concepto 990 **nunca depende de 970 ni de 980**; agrega los mismos conceptos hoja que ellos.

```
EARNING leaf ──────────►  970 (invert=false)
                    └────►  990 (invert=false)

DEDUCTION leaf ─────────►  980 (invert=false)
                    └────►  990 (invert=true)
```

Esto evita dependencias en cascada entre agregados y garantiza que el grafo de cálculo sea siempre un DAG sin nodos intermedios de agregado encadenados.

#### Activación explícita

Los conceptos AGGREGATE **no se activan automáticamente** por el hecho de que sus fuentes estén activadas. Requieren una fila explícita en `payroll_object_activation`, igual que el resto de conceptos.

#### Características
- No opera sobre datos primarios, sino sobre resultados previos.
- Representa composición o acumulación con signo controlado a nivel de relación.

#### Ejemplos
- total devengos (970)
- total deducciones (980)
- líquido a pagar / neto (990)
- bases de cotización
- bases fiscales

#### Nota importante
La estrategia de agregación con signo queda definida aquí. La forma de registrar las relaciones de feed y persistirlas se trata en los ADRs de infraestructura del motor de nómina.

---

## `FunctionalNature` para conceptos agregados totales

Los conceptos de tipo `AGGREGATE` que representan totales de nómina reciben valores específicos en el enum `FunctionalNature` para que el frontend pueda distinguirlos de las líneas de detalle al renderizar el recibo de salario.

Se añaden los siguientes valores al enum:

| Valor              | Semántica                                      | Concepto típico |
|--------------------|------------------------------------------------|-----------------|
| `TOTAL_EARNING`    | Suma de todos los devengos                     | 970             |
| `TOTAL_DEDUCTION`  | Suma de todas las deducciones                  | 980             |
| `NET_PAY`          | Líquido a pagar (devengos − deducciones)       | 990             |

Estos tres valores coexisten con los valores preexistentes (`EARNING`, `DEDUCTION`, `BASE`, `INFORMATIONAL`).

La `FunctionalNature` es un atributo de presentación/semántica del concepto; no altera la lógica de cálculo. Un concepto con `calculationType = AGGREGATE` y `functionalNature = NET_PAY` ejecuta exactamente la misma operación `SUM(feed_i × sign_i)` que cualquier otro AGGREGATE.

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
- la API de configuración.

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