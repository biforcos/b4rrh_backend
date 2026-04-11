# ADR — Payroll Root Model (`payroll.payroll`)

## Estado
Propuesto

## Contexto
B4RRHH organiza por verticales y usa business keys en APIs. `employee.presence` identifica una relación laboral (empleado + presenceNumber).
La nómina debe modelarse como **resultado de cálculo**, no como documento ni CRUD editable.

## Decisión
Se crea el bounded context `payroll` (schema propio) y la raíz:
- `payroll.payroll`

Representa el resultado funcional de una nómina para:
- empleado
- período de nómina
- tipo de nómina
- presencia

No es:
- documento PDF
- recurso editable
- entidad corregible in place

## Identidad funcional
- ruleSystemCode
- employeeTypeCode
- employeeNumber
- payrollPeriodCode
- payrollTypeCode
- presenceNumber

Ejemplo:
ESP + EMP + 0001 + 202501 + ORD + 2

## Campos raíz
- status
- statusReasonCode
- calculatedAt
- calculationEngineCode
- calculationEngineVersion

(No incluir totales agregados ni notas)

## Recursos hijos
### payroll_concept
- lineNumber
- conceptCode
- conceptLabel
- amount
- quantity?
- rate?
- conceptNatureCode
- originPeriodCode?
- displayOrder

### payroll_context_snapshot
- snapshotTypeCode
- sourceVerticalCode
- sourceBusinessKeyJson
- snapshotPayloadJson

## Reglas
- FK hijas con ON DELETE CASCADE
- No edición manual
- Sustitución por borrado + recreación
- Unicidad por business key

## Resumen
`payroll.payroll` es un resultado materializado, no editable, regenerable por cálculo, con conceptos y snapshots dependientes.
