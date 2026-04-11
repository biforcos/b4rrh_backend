# ADR — Payroll Status Workflow and Recalculation Guardrails

## Estado
Propuesto

## Contexto
Se necesita controlar estrictamente cuándo una nómina puede recalcularse.

## Estados
- NOT_VALID
- CALCULATED
- EXPLICIT_VALIDATED
- DEFINITIVE

## Regla central
Solo las nóminas en `NOT_VALID` pueden ser recalculadas (borradas y recreadas).

## Semántica

### NOT_VALID
- Resultado inválido o invalidado manualmente
- Único estado recalcable

### CALCULATED
- Resultado válido provisional
- No recalculable sin pasar a NOT_VALID

### EXPLICIT_VALIDATED
- Validada manualmente
- Bloqueada frente a recálculo automático

### DEFINITIVE
- Final, inmutable

## Transiciones

### Desde NOT_VALID
- -> CALCULATED (cálculo OK)
- -> NOT_VALID (cálculo sigue inválido)
- NO -> EXPLICIT_VALIDATED
- NO -> DEFINITIVE

### Desde CALCULATED
- -> NOT_VALID (invalidación)
- -> EXPLICIT_VALIDATED
- -> DEFINITIVE

### Desde EXPLICIT_VALIDATED
- -> NOT_VALID (manual)
- -> DEFINITIVE

### Desde DEFINITIVE
- sin salida

## statusReasonCode
Ejemplos:
- ENGINE_INVALID
- USER_INVALIDATED
- MASS_RECALC_REQUEST

## Regla operativa
El motor solo borra/recrea nóminas en NOT_VALID.

## Resumen
Workflow seguro que evita recálculos accidentales mediante invalidación explícita previa.
