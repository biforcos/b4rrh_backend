# ADR-020 — Cambio canónico de work center mediante replace-from-date

## Estado
Propuesto

## Contexto

B4RRHH modela sus recursos de empleado mediante verticales funcionales historizadas y APIs públicas basadas en business keys del empleado:

- ruleSystemCode
- employeeTypeCode
- employeeNumber

Dentro del mapa actual de verticales, `employee.work_center` se clasifica como una vertical temporal con restricciones de:

- no solape
- contención dentro de presence
- una única asignación vigente compatible en cada fecha

Aunque inicialmente puede existir una operación canónica de creación de work center, la experiencia real de uso ha demostrado que el cambio funcional habitual de centro de trabajo no puede modelarse de forma segura como un simple `create` aislado cuando ya existe una asignación abierta.

En un escenario real, si un empleado ya tiene un work center vigente y se desea cambiarlo con fecha efectiva X:

- no puede abrirse uno nuevo en X dejando el anterior abierto
- el anterior debe cerrarse en X - 1
- el nuevo debe comenzar en X

Por tanto, la operación funcional real no es “añadir otra fila”, sino **sustituir la ventana vigente desde una fecha**.

Esta necesidad se ha hecho visible especialmente al ejecutar simulación masiva con `workforce_loader`, donde la operación de creación directa genera conflictos funcionales que no aparecían en pruebas pequeñas.

## Problema

Usar únicamente una operación de creación para representar un cambio de work center provoca varios riesgos:

- solapes temporales
- necesidad de que el consumidor implemente lógica de cierre previa
- duplicación de semántica de dominio fuera del backend
- inconsistencias entre consumidores (frontend, loader, workflows)

Esto es contrario a la estrategia del proyecto, donde:

- el backend debe exponer operaciones canónicas de negocio
- el consumidor no debe reconstruir reglas temporales complejas por su cuenta

## Decisión

Se introduce una operación canónica para `employee.work_center` orientada a cambio funcional por fecha efectiva:

## `replace-from-date`

Su semántica será:

1. localizar la asignación de work center vigente en la fecha efectiva, si existe;
2. cerrarla en `effectiveDate - 1`;
3. crear la nueva asignación desde `effectiveDate`;
4. validar no solape, contención en presence y coherencia temporal completa.

## Naturaleza de la operación

`replace-from-date`:

- no es un CRUD genérico
- no sustituye a la operación de creación inicial
- representa el cambio funcional habitual de centro de trabajo

## API propuesta

```text
POST /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/work-centers/replace-from-date
```

Request mínima orientativa
{
  "effectiveDate": "2026-04-01",
  "workCenterCode": "WC02"
}
Reglas de negocio
1. Contención en presence

La nueva asignación debe estar contenida dentro de una presence válida del empleado.

2. No solape

No puede quedar más de una asignación de work center incompatible en la misma fecha.

3. Cierre implícito de la vigente

Si existe una asignación vigente en effectiveDate, debe cerrarse en effectiveDate - 1.

4. Creación de nueva asignación

La nueva asignación comienza en effectiveDate.

5. Operación idempotente semántica no requerida

No se exige idempotencia funcional estricta en V1, pero sí una validación clara de conflictos.

Consecuencias
Positivas
expresa la semántica real del cambio de centro
evita que frontend o loader implementen lógica temporal propia
mantiene la coherencia con otras operaciones temporales del sistema
facilita simulación masiva y workflows futuros
Negativas
añade una operación específica más al vertical
obliga a definir claramente el comportamiento cuando no existe work center vigente
Decisión de alcance para V1

En V1:

si existe work center vigente en la fecha efectiva, se cierra y se crea el nuevo
si no existe vigente, la operación podrá crear directamente la nueva asignación siempre que el contexto temporal sea válido
no se introducen todavía estrategias avanzadas de corrección administrativa
Relación con otras verticales

Esta decisión acerca work_center a un patrón de sustitución temporal por fecha efectiva, aunque sin convertirlo automáticamente en STRONG_TIMELINE.

No se afirma que work_center y contract sean idénticos como verticales, pero sí que ambos requieren una operación canónica de sustitución temporal cuando el cambio funcional afecta a una asignación vigente.
