# ADR-034 — Modelo semántico de `PayrollConcept`

## Estado
Propuesto

---

## Contexto

El ADR-033 introduce `PayrollObject` como raíz metamodelo canónica del motor de nómina y fija su identidad funcional común mediante la business key:

- `ruleSystemCode`
- `objectTypeCode`
- `objectCode`

Dentro de ese metamodelo común, el tipo de objeto `CONCEPT` requiere un modelo semántico propio que permita distinguir claramente:

- la identidad común heredada de `PayrollObject`
- la naturaleza estable del concepto de nómina
- las características mutables o versionables (definidas en ADRs posteriores)

Sin esta separación, existe el riesgo de mezclar en un mismo nivel:

- identidad
- tipo de cálculo
- presentación en recibo
- parámetros de cálculo
- fuentes de datos
- efectos funcionales

Esto dificultaría la trazabilidad, la retroactividad y la comprensión funcional del sistema de nómina.

El proyecto B4RRHH sigue principios claros:

- uso de business keys funcionales
- separación entre identidad estable y detalle mutable
- naming orientado a negocio
- evitar IDs técnicos en APIs

---

## Decisión

Se introduce `PayrollConcept` como subtipo semántico de `PayrollObject`.

`PayrollConcept` **no define una business key propia**.  
Hereda la identidad canónica de `PayrollObject`:

- `ruleSystemCode`
- `objectTypeCode`
- `objectCode`

Cuando `objectTypeCode = CONCEPT`, `objectCode` podrá nombrarse como `conceptCode` a nivel semántico, sin crear una nueva identidad.

---

## Propiedades maestras de `PayrollConcept`

Las siguientes propiedades definen la **naturaleza semántica estable** del concepto:

- `conceptMnemonic`
- `calculationType`
- `functionalNature`
- `resultCompositionMode`
- `payslipOrderCode`

El modelo queda preparado para incorporar en el futuro:

- `functionalSubnature` (clasificación funcional secundaria)

---

## Significado de las propiedades

### `conceptMnemonic`
Alias semántico legible del concepto.

Uso:
- reglas
- documentación
- trazabilidad
- debugging

No forma parte de la business key.

---

### `calculationType`
Define la naturaleza del cálculo del concepto.

Ejemplos:

- `DIRECT_AMOUNT`
- `QUANTITY_BY_RATE`
- `PRESENCE_VALUED`
- `AGGREGATE`
- `TECHNICAL_DERIVED`

---

### `functionalNature`
Define el papel funcional dentro de la nómina.

Valores iniciales:

- `EARNING`
- `DEDUCTION`
- `EMPLOYER_CHARGE`
- `BASE`
- `TOTAL`
- `TECHNICAL`

---

### `resultCompositionMode`
Define cómo se combinan múltiples resultados parciales del concepto dentro de una nómina.

Evita asumir que siempre debe existir una única línea final.

---

### `payslipOrderCode`

Define la posición lógica en el recibo.

Reglas:

- `NULL` → no se muestra en recibo
- valor informado → se muestra

Ordenación:


payslipOrderCode + objectCode


Sustituye conceptualmente a un `visibleInPayslip`.

---

## Regla crítica de inmutabilidad

`calculationType` es **inmutable**.

Si un concepto cambia su naturaleza de cálculo:

➡️ **NO se versiona**  
➡️ **Se crea un concepto nuevo**

Motivo:

- coherencia histórica
- retroactividad fiable
- trazabilidad clara
- comprensión funcional

---

## Consecuencias

### Positivas

- separación clara entre identidad y semántica
- modelo más robusto frente a cambios
- mejor trazabilidad y retro
- base sólida para evolución futura
- coherencia con arquitectura B4RRHH

---

### Costes

- algunos cambios requieren nuevos conceptos
- mayor disciplina de modelado
- separación más estricta entre semántica y parametrización

---

## No objetivos

Este ADR **NO define**:

- versionado de conceptos
- reglas de cálculo
- segmentación intrames
- relaciones con tablas o constantes
- implementación en BBDD
- APIs

---

## Resumen ejecutivo

`PayrollConcept` es un subtipo semántico de `PayrollObject` sin identidad propia adicional.

Su núcleo estable está formado por:

- `conceptMnemonic`
- `calculationType`
- `functionalNature`
- `resultCompositionMode`
- `payslipOrderCode`

`calculationType` es inmutable.

Los cambios en la naturaleza del concepto implican la creación de un nuevo concepto.