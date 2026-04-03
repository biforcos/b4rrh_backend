ADR — Employee Lifecycle Workflow: Hire Employee V1
Estado

Propuesto

Contexto

B4RRHH modela el empleado mediante una arquitectura basada en verticales independientes (presence, work_center, cost_center, contract, etc.), todas ellas accesibles mediante APIs por business key:

ruleSystemCode
employeeTypeCode
employeeNumber

Sin embargo, el ciclo de vida del empleado no se corresponde con la manipulación aislada de estas verticales, sino con acciones de negocio compuestas como:

contratar
terminar
recontratar

Estas acciones implican:

creación coordinada de múltiples verticales
coherencia temporal
validaciones transversales
una semántica funcional única

El ADR de lifecycle ya establece que estas acciones deben modelarse como workflows de negocio, no como secuencias de operaciones CRUD.

Además, la arquitectura de frontend define que estas acciones deben exponerse como WORKFLOW, no como edición genérica de datos.

Problema

Actualmente el sistema permite:

crear employee
crear presence
crear asignaciones organizativas
etc.

pero no existe una operación unificada de contratación.

Esto implica:

mala UX (el usuario tiene que ensamblar el empleado manualmente)
riesgo de inconsistencias temporales
pérdida de semántica de negocio
dificultad para evolucionar el lifecycle
Decisión

Se introduce el workflow:

Hire Employee V1

como una operación de negocio compuesta que:

crea el empleado
inicializa su relación laboral
establece su contexto organizativo inicial
garantiza coherencia temporal completa

Todo ello en una única operación orquestada.

Principios de diseño
1. Orientado a intención de negocio

El usuario no crea recursos técnicos.

El usuario ejecuta:

“Contratar empleado”

2. Presence como eje del lifecycle

El lifecycle del empleado se representa mediante presence.

Por tanto:

el Hire crea la primera presence
sin presence no hay relación laboral
3. Fecha central única

Se define:

hireDate es la fecha efectiva central del workflow

Regla:

Todas las entidades creadas deben compartir coherencia temporal:

presence.startDate = hireDate
work_center.startDate = hireDate
cost_center.startDate = hireDate
contract.startDate = hireDate
labor_classification.startDate = hireDate

No se permiten fechas divergentes en V1.

4. Orquestación única

El workflow:

ejecuta múltiples operaciones internas
se expone como una única operación externa
garantiza consistencia funcional
5. Sin exposición de IDs técnicos

La API:

usa exclusivamente business keys
no expone IDs internos
no mezcla identidades técnicas
6. Backend interpreta la semántica

El backend:

decide qué significa “HIRE”
construye el estado resultante
prepara los datos para UI

El frontend no deduce semántica compleja.

Alcance V1
Incluido

El workflow crea:

Employee core
Primera presence
Asignación organizativa inicial:
work center
cost center (opcional en V1)
Relación laboral inicial:
contract
labor classification
No incluido en V1
contactos
direcciones
identificadores
correcciones avanzadas
escenarios multi-fecha
edición parcial del workflow
API
Endpoint

Opción recomendada:

POST /employee-lifecycle/hire

Alternativa válida:

POST /employees/hire
Request

Ejemplo:

{
  "ruleSystemCode": "ESP",
  "employeeTypeCode": "EMP",
  "employeeNumber": "000123",

  "firstName": "Juan",
  "lastName1": "Pérez",
  "lastName2": "García",
  "preferredName": "Juan",

  "hireDate": "2026-04-01",
  "entryReasonCode": "HIRING",
  "companyCode": "COMP01",

  "workCenterCode": "WC01",

  "costCenterDistribution": {
    "items": [
      {
        "costCenterCode": "CC01",
        "allocationPercentage": 100
      }
    ]
  },

  "contract": {
    "contractTypeCode": "FULL",
    "contractSubtypeCode": "STD"
  },

  "laborClassification": {
    "agreementCode": "AGR01",
    "agreementCategoryCode": "CAT01"
  }
}
Response

Debe devolver un estado agregado listo para UI:

{
  "employee": {
    "ruleSystemCode": "ESP",
    "employeeTypeCode": "EMP",
    "employeeNumber": "000123",
    "displayName": "Juan Pérez García",
    "status": "ACTIVE"
  },
  "presence": {
    "startDate": "2026-04-01",
    "companyCode": "COMP01"
  },
  "workCenter": {
    "workCenterCode": "WC01"
  },
  "costCenter": {
    "startDate": "2026-04-01",
    "items": [...]
  },
  "contract": {...},
  "laborClassification": {...}
}
Validaciones
1. Employee
no debe existir previamente
si existe → 409 Conflict
2. Catálogos

Validar:

companyCode
workCenterCode
costCenterCode
entryReasonCode
contractType/subtype
agreement/category
3. Relaciones dependientes
agreementCategory depende de agreement
contractSubtype depende de contractType
etc.
4. Presence
debe crearse correctamente
no puede haber otra presence activa
5. Cost Center
suma <= 100
misma startDate
catálogo válido
contenido en presence
6. Coherencia temporal
todas las entidades deben respetar hireDate
no se permiten offsets en V1
Orquestación interna

Orden recomendado:

validar request
validar catálogos
validar dependencias
crear employee
crear presence
crear work center
crear cost center (si viene)
crear contract
crear labor classification
construir response

Todo dentro de un único servicio de aplicación.

Relación con TERMINATION

Este workflow deja al empleado en estado:

presence activa
asignaciones activas

Lo que permite que:

TERMINATION cierre correctamente todas las verticales activas

Sin estados intermedios incoherentes.

Relación con REHIRE

Diferencias clave:

Aspecto	Hire	Rehire
Employee	se crea	ya existe
Presence	primera	nueva
Histórico	vacío	preservado
Relación con Journey

El Hire debe generar un evento:

HIRE

El backend es responsable de esta interpretación.

El frontend no debe inferirlo.

Frontend

El workflow:

se expone como acción principal: “Contratar”
se implementa como pantalla dedicada
no como modal simple
no como formulario genérico

Patrón:

WORKFLOW
no SLOT
no TEMPORAL_APPEND_CLOSE
Consecuencias positivas
UX alineada con negocio
consistencia temporal garantizada
reducción de errores
base sólida para lifecycle completo
integración natural con Journey
Costes
mayor complejidad en capa application
necesidad de validaciones transversales
mayor esfuerzo inicial
Alternativas descartadas
CRUD por vertical

Descartado:

no refleja dominio
propenso a inconsistencias
Employee como agregado monolítico

Descartado:

rompe arquitectura vertical
reduce flexibilidad
Workflow parcial

Descartado:

deja estados intermedios inválidos
Resumen

Hire Employee V1 introduce una operación de negocio compuesta que:

crea el empleado
establece su relación laboral
define su contexto organizativo inicial
garantiza coherencia temporal

Todo ello en una única operación orquestada, alineada con el modelo de verticales y con el ciclo de vida real del empleado.