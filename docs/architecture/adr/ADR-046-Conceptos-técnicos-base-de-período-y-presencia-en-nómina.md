# ADR-046 — Conceptos técnicos base de período y presencia en nómina

## Estado

Aceptado.

## Contexto

El motor de nómina de B4RRHH ya permite calcular conceptos económicos y agregados como:

- SALARIO_BASE
- TOTAL_DEVENGOS
- TOTAL_DEDUCCIONES
- NETO

Sin embargo, algunos valores necesarios para calcular correctamente una nómina mensual todavía están implícitos o calculados en duro.

Ejemplos:

- días reales del mes
- días teóricos de nómina
- días de presencia del empleado en el período
- días de presencia por subperíodo o segmento

Estos valores no son conceptos económicos visibles en el recibo, pero sí son datos fundamentales para explicar y trazar el cálculo.

## Problema

Si estos valores permanecen ocultos dentro del cálculo:

- el salario base no es completamente trazable;
- se dificulta depurar altas, bajas y meses incompletos;
- se complica evolucionar hacia cálculo por tramos;
- no queda claro qué cantidad de días ha alimentado cada concepto económico.

Además, no todos los conceptos del motor pueden depender de otros conceptos configurados.

En algún punto existen conceptos fundamentales que nacen directamente del contexto de ejecución.

## Decisión

Se introducen conceptos técnicos base de nómina con la nomenclatura D01/D02/D03, más cómoda para negocio.

Estos conceptos:

- se calculan durante la ejecución mediante clases Java específicas (`CalculationType.JAVA_PROVIDED`);
- tienen `FunctionalNature.TECHNICAL` para distinguirlos de conceptos económicos;
- pueden alimentar otros conceptos como operandos del grafo;
- no se muestran en el recibo de nómina (`payslipOrderCode = null`);
- no representan devengos ni deducciones.

## Conceptos técnicos

### D01 — DIAS_DEVENGO

Días de devengo mensual del segmento. Valor que se usa como operando QUANTITY de SALARIO_BASE.

Regla V1:

```
D01 = min(daysInSegment, 30)
```

Ejemplos:

- empleado activo todo abril (30 días): 30
- empleado activo todo marzo (31 días): 30 (topado)
- alta el 10 de marzo (22 días en segmento): 22
- baja el 20 de febrero (20 días en segmento): 20

Sustituye al anterior D01 (DIRECT_AMOUNT, CONSTANT=30 fijo), que no contemplaba meses parciales.

### D02 — DIAS_MES_NOMINA

Días teóricos del mes de nómina. Denominador convencional para el cálculo mensual.

Regla V1:

```
D02 = 30 (siempre)
```

Febrero, meses de 31 días y cualquier mes se tratan como 30 a efectos de nómina mensual.

### D03 — DIAS_MES_REALES

Días naturales reales del mes de cálculo.

Regla V1:

```
D03 = periodStart.lengthOfMonth()
```

Ejemplos:

- enero: 31
- febrero: 28 o 29 (año bisiesto)
- abril: 30

## Relación con SALARIO_BASE

Para salario base mensual V1:

```
SALARIO_BASE = D01 × P01
```

Donde `P01` (PRECIO_DIA) se obtiene de tabla de tarifas por categoría de convenio.

Con D01 dinámico por segmento, el empleado con alta el 10 de marzo cobra proporcionalmente
(`22 × P01`) en lugar del mes completo (`30 × P01`).

## Implementación

`CalculationType.JAVA_PROVIDED` identifica estos conceptos en el grafo. El `DefaultSegmentExecutionEngine`
y el `CalculatePayrollUnitService` despachan a la implementación registrada por Spring mediante
`List<TechnicalConceptCalculator>`.

Calculadores registrados:

| Concepto | Clase                              |
|----------|------------------------------------|
| D01      | `AccrualDaysConceptCalculator`     |
| D02      | `PayrollMonthDaysConceptCalculator`|
| D03      | `CalendarDaysConceptCalculator`    |

La interfaz `TechnicalConceptCalculator` recibe un `TechnicalConceptSegmentData` (solo fechas y
`daysInSegment`) para mantener el contrato estrecho y fácilmente testeable.

## Regla de visibilidad

Los conceptos técnicos base tienen `payslipOrderCode = null`. No se persisten como líneas de recibo.
Son calculados y disponibles en el estado de ejecución para ser operandos de otros conceptos.

## Regla arquitectónica

Las clases `TechnicalConceptCalculator` no deben convertirse en un motor paralelo.

Solo pueden calcular conceptos técnicos fundamentales derivados de:

- período de nómina;
- calendario mensual;
- presencia del empleado;
- segmentos temporales efectivos.

No deben calcular conceptos económicos como salario base, antigüedad, nocturnidad, IRPF, totales o neto.

## Segmentación futura

Cuando exista cálculo por subperíodos, D01 ya está preparado: opera sobre `daysInSegment`
(el segmento activo), no sobre `daysInPeriod`. D02 y D03 son invariantes del período y
devuelven el mismo valor en todos los segmentos de un mismo mes.

## Consecuencias positivas

- Mejora la trazabilidad del cálculo de SALARIO_BASE.
- Elimina el hardcode de `D01_FIXED_30`.
- Prepara el motor para altas, bajas y cambios de situación dentro del mes.
- Permite explicar el salario base con meses parciales.
- Mantiene limpio el recibo.
- Encaja con el grafo de conceptos sin forzar que todo sea configurable.

## Riesgos

**1. Abusar de JAVA_PROVIDED**

No todo helper interno debe convertirse en concepto técnico. Solo deben modelarse
valores relevantes para explicar el cálculo.

**2. Crear un segundo motor en Java**

Las clases técnicas no deben calcular conceptos económicos.

**3. Naming ambiguo**

Se adopta la nomenclatura D01/D02/D03 por ser más cómoda para negocio y no generar
confusión entre DIAS_MES_REALES (D03) y DIAS_MES_NOMINA (D02).
