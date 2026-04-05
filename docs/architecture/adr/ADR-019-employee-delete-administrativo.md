# ADR-019 — Borrado administrativo de employee con cascada técnica controlada

## Estado
Propuesto

## Contexto

B4RRHH modela el dominio de empleado mediante:

- identidad pública por business key:
  - ruleSystemCode
  - employeeTypeCode
  - employeeNumber
- verticales hijas funcionales del empleado
- lifecycle ordinario basado en:
  - hire
  - terminate
  - rehire

Además, el proyecto distingue entre:

- operaciones funcionales normales del ciclo de vida
- operaciones administrativas o técnicas excepcionales

Hasta ahora, el modelo conceptual del recurso `employee.employee` no se ha orientado al borrado como operación canónica de negocio, sino a conservación de identidad e histórico. Sin embargo, existen escenarios legítimos donde un borrado administrativo sí tiene sentido, por ejemplo:

- alta creada por error
- empleado que finalmente no llega a incorporarse
- datos de prueba o limpieza controlada de entornos no productivos
- reversión temprana de una contratación todavía sin efectos descendentes relevantes

Al mismo tiempo, no se quiere permitir un borrado indiscriminado ni delegar toda la semántica del delete a la base de datos.

Se necesita una decisión explícita sobre:

- existencia o no de endpoint de borrado
- naturaleza funcional de ese borrado
- relación entre validación de aplicación y cascada física en persistencia
- preparación del modelo para futuras restricciones de elegibilidad

## Decisión

Se introduce una operación explícita de **borrado administrativo de employee**.

### Naturaleza de la operación

El borrado de employee:

- **no forma parte del lifecycle ordinario**
- **no sustituye a terminate**
- **no representa un flujo funcional normal**
- se modela como una **operación administrativa excepcional**

### Identidad del endpoint

La operación se expone por business key del empleado:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber`

### Regla inicial de elegibilidad (V1)

En esta primera versión, el borrado se permitirá únicamente cuando:

- el empleado exista

Si el empleado no existe:

- la operación devolverá `404 Not Found`

### Reglas futuras explícitamente previstas

Aunque en V1 no se aplican bloqueos funcionales adicionales, esta operación queda diseñada para soportar en el futuro validaciones como:

- no permitir borrado si el empleado tiene nómina calculada
- no permitir borrado si existen efectos descendentes relevantes
- no permitir borrado si el empleado ya superó cierto punto funcional del ciclo de vida
- otras reglas de elegibilidad administrativa

Si en el futuro una regla impide el borrado:

- la operación deberá devolver `409 Conflict`

## Persistencia

Cuando el borrado sea autorizado por la capa de aplicación, la eliminación física del empleado podrá apoyarse en **cascada técnica de base de datos** sobre las verticales hijas dependientes por `employee_id`.

Principio:

- la **aplicación decide si se puede borrar**
- la **base de datos ejecuta el borrado relacional completo**

La cascada en base de datos se considera una decisión de persistencia y consistencia técnica, no una definición de semántica de negocio.

## API propuesta

```text
DELETE /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}