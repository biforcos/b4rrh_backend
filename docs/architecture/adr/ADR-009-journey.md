Design Principles
1. Journey debe contar la historia del empleado

Un journey debe responder a:

qué pasó

cuándo pasó

cómo se interpreta funcionalmente

por qué ese evento importa

No debe limitarse a agrupar verticales en paralelo.

2. El frontend no debe inferir semántica de negocio compleja

El frontend no debe deducir por sí solo si algo es:

un alta

una recontratación

un cambio de contrato

un cambio de clasificación

Esa interpretación debe resolverse en backend, dentro del read model.

3. Journey es una proyección read-only de UI

Journey V2 no sustituye:

verticales canónicas

endpoints de escritura

recursos de dominio independientes

Es una proyección agregada para experiencia de usuario.

4. Tracks siguen teniendo valor

La vista actual por tracks sigue siendo útil para:

inspección técnica

validación funcional

representación por vertical

Por tanto, no debe considerarse un fracaso ni descartarse.

Current Model Reclassification

El modelo actualmente expuesto bajo journey debe reinterpretarse como:

Employee Tracks

Employee History Tracks

o naming equivalente

No se recomienda seguir llamándolo “journey” en su estado actual.

Journey V2 — Target Model
Shape propuesto
{
  "employee": {
    "ruleSystemCode": "ESP",
    "employeeTypeCode": "INTERNAL",
    "employeeNumber": "EMP010",
    "displayName": "Juan Antonio Biforcos Amor"
  },
  "events": [
    {
      "eventDate": "2023-01-10",
      "eventType": "HIRE",
      "trackCode": "PRESENCE",
      "title": "Alta en la empresa",
      "subtitle": "ES01 · período #1",
      "status": "completed",
      "isCurrent": false,
      "details": {
        "companyCode": "ES01",
        "entryReasonCode": "HIRING"
      }
    }
  ]
}
Event Model

Cada evento debe incluir, como mínimo:

eventDate

eventType

trackCode

title

subtitle

status

isCurrent

details

Campo eventDate

Fecha efectiva del evento en la línea temporal.

Campo eventType

Tipo de evento normalizado y entendible para frontend.

Campo trackCode

Origen funcional del evento:

PRESENCE

CONTRACT

LABOR_CLASSIFICATION

WORK_CENTER

COST_CENTER

etc.

Campo title

Texto principal listo para UI.

Campo subtitle

Contexto breve y útil para lectura rápida.

Campo status

Estado visual del evento:

completed

current

future

u otro conjunto acotado

Campo isCurrent

Flag explícito para eventos actualmente vigentes o activos.

Campo details

Información adicional libre y limitada, para tooltips, badges o ampliación contextual.

Initial Event Types
Presence

HIRE

REHIRE

TERMINATION

PRESENCE_START

PRESENCE_END

Contract

CONTRACT_START

CONTRACT_CHANGE

CONTRACT_END

Labor Classification

LABOR_CLASSIFICATION_START

LABOR_CLASSIFICATION_CHANGE

LABOR_CLASSIFICATION_END

Future

WORK_CENTER_CHANGE

COST_CENTER_CHANGE

ASSIGNMENT_CHANGE

Backend Interpretation Rules

El backend debe encargarse de transformar ocurrencias de verticales en eventos funcionales.

Ejemplos

una nueva presence con motivo de entrada inicial puede generar HIRE

una nueva presence posterior a una terminación puede generar REHIRE

el cierre de una presence con motivo adecuado puede generar TERMINATION

un cambio de contrato genera CONTRACT_CHANGE

una nueva clasificación laboral genera LABOR_CLASSIFICATION_CHANGE

El frontend no debe deducir esta semántica a partir de details.

Relationship with Lifecycle Workflows

Journey V2 se alinea directamente con el ADR de lifecycle workflows:

Hire

Terminate

Rehire

Esto permite que la timeline agregada represente:

estados

transiciones

hitos funcionales del ciclo de vida del empleado

y no solo snapshots por vertical.

UI Considerations
Objetivo de Journey V2

Permitir una representación clara de:

la historia laboral del empleado

eventos relevantes

cambios significativos

estado actual dentro de la secuencia

Recomendación visual

Se recomienda una timeline orientada a eventos cronológicos, preferentemente:

vertical

o híbrida compacta

No se considera óptimo reutilizar directamente la representación actual por tracks para este objetivo.

Tracks en UI

La vista por tracks puede seguir existiendo como:

vista técnica

vista avanzada

o modo alternativo de inspección

Pero no como definición principal de “journey”.

Migration Strategy
Phase 1 — Reclassify current model

aceptar que el modelo actual es tracks

ajustar naming interno/documentación si procede

mantener compatibilidad

Phase 2 — Design Journey V2 contract

definir EmployeeJourneyV2Response

definir catálogo inicial de eventType

acordar reglas de agregación en backend

Phase 3 — Implement backend projection

construir la proyección events[]

reutilizando verticales existentes

sin alterar recursos canónicos

Phase 4 — Adapt frontend

nuevo client / mapper / store / UI para journey V2

timeline realmente cronológica

dejar la vista actual por tracks como opcional o técnica

Consequences
Positivas

naming más honesto

mejor alineación semántica

journey verdaderamente útil para UI

menos lógica interpretativa en frontend

mejor encaje con lifecycle workflows

Negativas / Costes

hay que diseñar un segundo read model

el backend debe añadir reglas de interpretación

puede convivir temporalmente más de una proyección agregada

habrá que ajustar frontend para el nuevo shape

Alternatives Considered
1. Mantener el shape actual y mejorarlo solo en frontend

Descartado como solución final:

obliga al frontend a inferir demasiada semántica

sigue sin representar bien un journey

2. Renombrar simplemente el endpoint actual y no hacer V2

Insuficiente:

arregla naming

no resuelve la necesidad de una timeline funcional de eventos

3. Convertir journey en agregado completo de dominio

Descartado:

journey debe seguir siendo una proyección read-only

no debe sustituir a verticales canónicas

Summary

El modelo actual no representa un journey, sino una vista histórica por tracks.

Se decide:

reclasificar conceptualmente el modelo actual como tracks

diseñar Journey V2 como una proyección cronológica de eventos

mantener separadas:

la vista técnica por tracks

la vista funcional de journey

Esto permitirá construir una timeline realmente útil para frontend, alineada con el ciclo de vida del empleado y con la semántica de negocio del dominio.