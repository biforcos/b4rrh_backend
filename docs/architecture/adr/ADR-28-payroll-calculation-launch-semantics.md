# ADR — Payroll Calculation Launch Semantics

## Estado
Propuesto

## Contexto

B4RRHH organiza el código por vertical/subdominio y exige APIs públicas basadas en business keys, nunca en IDs técnicos. Además, cuando una operación no encaja como CRUD plano, el proyecto favorece modelarla como una acción de negocio o workflow explícito. fileciteturn4file10 fileciteturn4file12 fileciteturn4file8

En el bounded context `payroll` ya se ha decidido que:

- la raíz funcional es `payroll.payroll`;
- su identidad funcional es:
  - `ruleSystemCode`
  - `employeeTypeCode`
  - `employeeNumber`
  - `payrollPeriodCode`
  - `payrollTypeCode`
  - `presenceNumber`;
- la nómina es un resultado materializado, no editable, regenerable por cálculo;
- las hijas cuelgan con `ON DELETE CASCADE`;
- solo `NOT_VALID` es estado recalcable entre las nóminas ya existentes. fileciteturn4file1 fileciteturn4file6

También se ha fijado que `employee.presence` es un recurso funcional identificado por business key ampliada `ruleSystemCode + employeeTypeCode + employeeNumber + presenceNumber`, y que las acciones de negocio compuestas deben vivir como workflows por encima de los recursos canónicos. fileciteturn4file14 fileciteturn4file16 fileciteturn4file8

Al empezar a hablar de cálculo de nómina aparece una tensión natural:

- una cosa es el **modelo de datos del resultado** (`payroll.payroll`);
- otra cosa distinta es el **lanzamiento del cálculo**.

Si ambas cosas se mezclan demasiado pronto, el diseño queda borroso y se dificulta la evolución futura del motor de reglas.

## Problema

Se necesita definir qué significa técnicamente “lanzar nómina” sin entrar todavía en el motor real de reglas de cálculo.

El sistema debe poder:

- recibir un período y un tipo de nómina;
- resolver una población objetivo;
- expandir esa población a unidades reales de cálculo;
- decidir cuáles son elegibles;
- delegar el cálculo efectivo a otro caso de uso especializado;
- devolver un resumen de ejecución.

Además, el launch no debe recalcular indiscriminadamente:

- una nómina existente en `CALCULATED` no debe tocarse;
- una nómina existente en `EXPLICIT_VALIDATED` no debe tocarse;
- una nómina `DEFINITIVE` jamás debe tocarse;
- una unidad sin nómina previa sí debe calcularse;
- una unidad con nómina previa en `NOT_VALID` sí debe recalcularse.

## Decisión

Se introduce la semántica de **Payroll Calculation Launch** como workflow de aplicación dentro del bounded context `payroll`.

El launch:

- **no es** la raíz funcional del dominio;
- **no es** un CRUD;
- **no es** todavía un recurso persistente canónico tipo `payroll_run`;
- **no implementa** por sí mismo el motor de cálculo;
- **resuelve y orquesta** qué unidades deben intentarse calcular.

### Regla principal

`launch` resuelve la lista de unidades de cálculo elegibles y delega el cálculo efectivo a un caso de uso/endpoint especializado de cálculo.

## Definición funcional

Lanzar nómina significa:

> ejecutar un workflow que, para un `ruleSystemCode`, `payrollPeriodCode`, `payrollTypeCode` y una población objetivo determinada, resuelve las unidades de cálculo candidato, considera elegibles las que no tienen nómina previa o la tienen en `NOT_VALID`, delega el cálculo efectivo a un componente especializado y devuelve un resumen de ejecución.

## Unidad funcional de cálculo

La unidad mínima de cálculo es:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber`
- `payrollPeriodCode`
- `payrollTypeCode`
- `presenceNumber`

Justificación:

- `payroll.payroll` ya está anclada a una presencia concreta; fileciteturn4file1
- `presence` tiene identidad pública propia dentro del empleado; fileciteturn4file14 fileciteturn4file16
- dos presencias distintas en el mismo mes representan nóminas independientes.

El launch trabaja con una colección de estas unidades, no con “empleados enteros” de forma opaca.

## Población objetivo vs población elegible

Se distinguen dos conceptos:

### 1. Población objetivo

Es el conjunto de empleados o ámbitos sobre los que el usuario desea lanzar el cálculo.

Ejemplos posibles:

- un empleado;
- una lista explícita de empleados;
- todos los empleados de un `ruleSystemCode`;
- futuros filtros más ricos.

### 2. Población elegible

Es el conjunto de unidades de cálculo que realmente pueden entrar al cálculo efectivo.

Una unidad es elegible si:

- **no existe** `payroll.payroll` para su business key funcional; o
- **existe** y su `status = NOT_VALID`.

Una unidad no es elegible si existe y su estado es:

- `CALCULATED`
- `EXPLICIT_VALIDATED`
- `DEFINITIVE`

## Responsabilidades del launch

El launch debe:

1. recibir el contexto de ejecución;
2. resolver la población objetivo;
3. expandirla a unidades de cálculo candidatas;
4. comprobar existencia y estado de `payroll.payroll`;
5. construir la lista final de unidades elegibles;
6. delegar el cálculo efectivo;
7. consolidar un resumen de ejecución.

El launch no debe:

- generar directamente conceptos de nómina;
- decidir reglas salariales;
- prorratear;
- aplicar retroactividad real;
- convertirse en el motor de cálculo.

## Contexto mínimo de ejecución

El launch debe trabajar al menos con:

- `ruleSystemCode`
- `payrollPeriodCode`
- `payrollTypeCode`
- `calculationEngineCode`
- `calculationEngineVersion`
- `targetSelection`

Los dos campos de engine son obligatorios por coherencia con el modelo raíz ya adoptado para `payroll.payroll`. fileciteturn4file1

## targetSelection

`targetSelection` representa la población objetivo.

No se fija todavía un único shape contractual cerrado, pero el modelo debe permitir al menos:

- cálculo de un empleado concreto;
- cálculo de una lista explícita;
- cálculo masivo por ámbito.

El diseño exacto del payload se cerrará en OpenAPI posterior.

## Delegación al cálculo efectivo

El launch no implementa el cálculo. Delegará en un caso de uso/endpoint especializado, en adelante `calculate`.

Esta separación permite:

- probar el flujo completo antes de tener motor real;
- evolucionar el componente de cálculo sin rediseñar el launch;
- distinguir claramente entre orquestación y cálculo.

## Resultado del launch

El launch debe devolver un resumen explícito de ejecución.

Campos esperables del resumen:

- total de candidatos detectados;
- total de unidades elegibles;
- total de unidades no elegibles por estado;
- total de unidades calculadas con resultado `CALCULATED`;
- total de unidades calculadas con resultado `NOT_VALID`;
- total de errores técnicos;
- detalle opcional por unidad.

No se decide todavía persistir este resumen como recurso canónico.

## Qué se rechaza explícitamente

Se rechaza en esta fase:

- modelar `launch` como CRUD;
- mezclar launch y cálculo efectivo en la misma semántica;
- recalcular cualquier nómina encontrada dentro de la población objetivo;
- introducir ya un `payroll_run` como centro del dominio;
- abrir todavía un repositorio/microservicio separado sólo para el cálculo.

## Relación con el workflow de estados

Este ADR no sustituye al ADR de estados de nómina.

Se complementa con él:

- `NOT_VALID` sigue siendo el estado que autoriza la sustitución de una nómina existente; fileciteturn4file0
- además, una unidad sin nómina previa es también elegible para cálculo.

## API conceptual inicial

A falta de OpenAPI definitivo, se recomienda un endpoint de negocio del estilo:

- `POST /payroll/calculations/launch`

El nombre debe seguir semántica de negocio, no nomenclatura técnica vaga. El proyecto prioriza nombres orientados a negocio y paths por business keys cuando aplica. fileciteturn4file12

## Consecuencias

### Positivas

- separa claramente modelo y proceso;
- permite probar el flujo completo sin motor real;
- protege de recálculos accidentales;
- deja abierta evolución futura del motor;
- encaja con el patrón del proyecto de workflows explícitos. fileciteturn4file8

### Costes

- introduce un caso de uso adicional;
- exige resolver correctamente la expansión de población a presencias;
- obliga a diseñar un resumen de ejecución útil.

## Resumen

En B4RRHH, `launch` no calcula la nómina por sí mismo.

`launch` es el workflow que:

- resuelve la población objetivo;
- expande a unidades reales de cálculo;
- considera elegibles las unidades sin nómina previa o con nómina `NOT_VALID`;
- delega el cálculo efectivo;
- devuelve un resumen explícito del proceso.
