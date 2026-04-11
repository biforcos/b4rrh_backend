# ADR — Payroll Calculate Contract (Initial Stub Calculator)

## Estado
Propuesto

## Contexto

B4RRHH ya ha decidido que `payroll.payroll` es un resultado materializado, no editable, con business key funcional basada en empleado + período + tipo + presencia, y que los estados del resultado gobiernan si una nómina puede o no ser sustituida. fileciteturn4file1 fileciteturn4file0

También se ha decidido ahora que el launch de nómina sólo resuelve y orquesta unidades elegibles, delegando el cálculo real a otro caso de uso especializado.

El proyecto, además, exige:

- arquitectura vertical-first;
- APIs públicas por business keys;
- naming semántico;
- evitar sobreingeniería prematura. fileciteturn4file10 fileciteturn4file12 fileciteturn4file13

En esta fase todavía no existe un motor de reglas de nómina real. Sin embargo, hace falta un componente de cálculo inicial que permita probar:

- el flujo launch -> calculate;
- la sustitución por borrado + recreación;
- la creación de `payroll.payroll`;
- la generación de conceptos;
- la generación de snapshots;
- el tratamiento de estados `CALCULATED` y `NOT_VALID`.

## Problema

Se necesita un contrato de cálculo inicial que permita construir un **stub calculator útil**, suficientemente real para validar el pipeline técnico y funcional, pero deliberadamente pequeño para no anticipar todavía el motor de reglas.

Ese cálculo inicial debe:

- recibir unidades explícitas ya resueltas;
- no decidir poblaciones objetivo;
- materializar resultados en `payroll`;
- poder generar resultados `CALCULATED` y `NOT_VALID`;
- ser sustituible en el futuro por el motor real sin romper la semántica externa.

## Decisión

Se introduce el contrato de **Payroll Calculate** como caso de uso/endpoint especializado que recibe una lista explícita de unidades de cálculo y materializa resultados de nómina.

`calculate`:

- no resuelve la población objetivo;
- no selecciona elegibles por sí mismo como responsabilidad principal;
- no es todavía un motor declarativo de reglas;
- actúa como calculador inicial del sistema;
- podrá empezar implementado como **stub calculator**.

## Definición funcional

`calculate` recibe una colección cerrada de unidades de cálculo y, para cada una de ellas:

1. valida precondiciones mínimas;
2. elimina la nómina previa sólo si existe y es sustituible según reglas;
3. crea una nueva `payroll.payroll`;
4. crea conceptos de nómina de prueba o cálculo básico;
5. crea snapshots contextuales mínimos;
6. persiste el resultado final en estado:
   - `CALCULATED`, si el cálculo concluye correctamente;
   - `NOT_VALID`, si detecta una invalidez funcional del cálculo.

## Unidad de entrada

La unidad mínima de entrada es:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber`
- `payrollPeriodCode`
- `payrollTypeCode`
- `presenceNumber`
- `calculationEngineCode`
- `calculationEngineVersion`

### Regla

`calculate` debe trabajar con unidades **explícitas**.

No debe aceptar un payload ambiguo que implique “resolver toda una población”. Esa responsabilidad pertenece al launch.

## Responsabilidades de calculate

`calculate` debe:

- cargar el contexto funcional mínimo de la unidad;
- comprobar la existencia previa de `payroll.payroll`;
- aplicar la política de sustitución;
- crear nueva raíz `payroll.payroll`;
- crear `payroll_concept`;
- crear `payroll_context_snapshot`;
- devolver resultado por unidad.

`calculate` no debe todavía:

- implementar reglas salariales complejas;
- modelar convenios reales;
- resolver retroactividad completa;
- introducir DSLs o engines genéricos;
- depender de un metamodelo complejo de reglas.

## Política de sustitución

Para cada unidad explícita:

### Si no existe nómina previa
- crear una nueva `payroll.payroll`.

### Si existe y está `NOT_VALID`
- eliminar la raíz previa;
- dejar que `ON DELETE CASCADE` elimine conceptos y snapshots; fileciteturn4file6
- crear una nueva `payroll.payroll`.

### Si existe y está `CALCULATED`, `EXPLICIT_VALIDATED` o `DEFINITIVE`
- no sustituirla;
- devolver resultado de unidad ignorada/no procesada, según shape final del contrato.

## Contrato de salida

`calculate` debe devolver resultado por unidad.

Campos orientativos:

- identity de la unidad;
- `processed = true/false`;
- `resultStatus = CALCULATED | NOT_VALID | SKIPPED`;
- motivo cuando no se procese;
- business key final de la nómina generada, si aplica.

## Stub calculator inicial

Se adopta explícitamente una estrategia de implementación por fases.

### Fase inicial permitida

El primer `calculate` puede generar una nómina artificial pero funcionalmente útil.

Ejemplo mínimo:

- crear `payroll.payroll`;
- generar 2 conceptos de prueba;
- generar 1 o 2 snapshots de contexto;
- persistir en `CALCULATED`.

También puede contemplarse una condición de prueba que genere `NOT_VALID` cuando falte algún dato mínimo requerido.

### Objetivo de esta fase

No hacer nómina real todavía.

El objetivo es validar el pipeline:

- endpoints;
- wiring;
- persistencia;
- borrado y recreación;
- estados;
- snapshots;
- conceptos;
- respuesta funcional.

## Conceptos de prueba

En esta fase, `payroll_concept` puede contener conceptos semilla o de demostración.

Ejemplo conceptual:

- `BASE_TEST`
- `DEVENGO_TEST`

Los nombres y códigos deben seguir una convención de negocio estable y no reforzar identidades técnicas equivocadas. El proyecto prioriza nombres de negocio y códigos funcionales estables. fileciteturn4file12

## Snapshots mínimos

`calculate` debe poblar al menos snapshots básicos para demostrar el diseño ya aprobado de `payroll_context_snapshot`. fileciteturn4file6

Ejemplos iniciales razonables:

- `EMPLOYEE_CORE`
- `PRESENCE`
- opcionalmente `WORKING_TIME` o `CONTRACT` cuando sea barato de recuperar

No es obligatorio arrancar con todos los snapshots futuros.

## Reglas de error / invalidez

En esta fase se distinguen dos clases:

### 1. Error técnico
Ejemplo:
- fallo de persistencia;
- error inesperado de infraestructura.

Esto debe reportarse como error técnico del proceso.

### 2. Resultado funcional `NOT_VALID`
Ejemplo:
- falta dato mínimo requerido para construir el cálculo stub;
- inconsistencia funcional detectada por el calculador.

En este caso sí puede persistirse una `payroll.payroll` con `status = NOT_VALID`, coherente con el workflow ya aprobado. fileciteturn4file0

## Forma de exposición

A falta de contrato final, se admiten dos estrategias:

### Opción A — calculate sólo como caso de uso interno
Útil si launch es el único endpoint externo.

### Opción B — calculate también como endpoint explícito
Útil para pruebas con Postman y validación incremental del pipeline.

En esta fase, se acepta la opción B por su valor práctico para acelerar aprendizaje y validación del flujo.

Nombre conceptual recomendado:

- `POST /payroll/calculations/calculate`

## Qué se rechaza explícitamente

Se rechaza en esta fase:

- introducir un motor declarativo de reglas;
- mezclar calculate con resolución de población;
- convertir calculate en un endpoint de “hazlo todo” sin unidades explícitas;
- bloquear el diseño futuro con un contrato demasiado acoplado al stub.

## Consecuencias

### Positivas

- permite probar el flujo completo desde muy pronto;
- separa orquestación de cálculo;
- facilita sustitución futura por motor real;
- valida conceptos y snapshots sin esperar al dominio salarial completo.

### Costes

- exige mantener disciplina para que el stub no se convierta en solución definitiva;
- habrá que evolucionar el contrato interno del calculador en fases posteriores;
- el primer resultado no representará todavía nómina real.

## Resumen

En B4RRHH, `calculate` es el caso de uso especializado que recibe unidades explícitas ya resueltas y materializa `payroll.payroll` con conceptos y snapshots.

En la primera iteración, puede implementarse como un **stub calculator útil**, orientado a validar el flujo técnico y funcional, no a resolver todavía el motor real de reglas de nómina.
