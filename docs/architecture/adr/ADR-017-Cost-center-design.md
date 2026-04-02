ADR — Employee Cost Center Vertical
Estado

Propuesto

Contexto

B4RRHH modela el dominio de empleado mediante verticales funcionales independientes dentro del bounded context employee, siguiendo arquitectura vertical-first, hexagonal interna y APIs públicas basadas exclusivamente en business keys. El empleado se identifica funcionalmente por:

ruleSystemCode
employeeTypeCode
employeeNumber

y los recursos hijos deben derivar su identidad desde esa business key, sin exponer IDs técnicos en la API pública.

En la evolución del mapa de verticales del proyecto, cost_center ya aparece identificado como una vertical de tipo:

DISTRIBUTED_TIMELINE
catálogo simple
reglas temporales: MULTI_ACTIVE + SUM<=100

y pendiente todavía de aterrizar operativamente.

Además, el lenguaje temporal común del proyecto distingue claramente entre:

STRONG_TIMELINE
FLEXIBLE_TIMELINE
DISTRIBUTED_TIMELINE

reservando para esta última los casos multi-activos y con reglas agregadas, típicamente basadas en porcentajes. El propio patrón de StrongTimelineReplacePlanner indica expresamente que no debe aplicarse directamente a cost_center, porque esa vertical no es single-active ni de cobertura completa.

También existe ya una decisión explícita de naming de catálogos reutilizables: cuando el concepto es transversal o reusable, el rule_entity_type debe nombrar el concepto funcional real. COST_CENTER aparece como ejemplo claro de catálogo reusable y además ya está previsto como binding directo para employee.cost_center / costCenterCode.

Por último, el dominio de lifecycle ya establece que TERMINATION debe cerrar o ajustar asignaciones vigentes del empleado, preservando el histórico y sin dejar restos abiertos tras la terminación.

Problema

employee.cost_center no encaja correctamente ni como:

CRUD plano por filas
ni como simple clon de work_center
ni como STRONG_TIMELINE

porque su semántica real no es “una única asignación activa”, sino una distribución organizativa que puede tener varias líneas simultáneas activas para una misma fecha.

Ejemplo funcional válido:

50% en CC_A
50% en CC_B

vigentes a la vez desde la misma fecha.

Esto introduce necesidades específicas que no aparecen en verticales single-active:

permitir multi-actividad simultánea;
impedir que la suma de porcentajes supere 100 en un momento dado;
evitar mezclas incoherentes de líneas paralelas con fechas de inicio distintas;
definir una unidad funcional de cambio más fuerte que “una fila aislada”;
aclarar cómo impacta TERMINATION;
definir operaciones canónicas honestas, evitando un CRUD fila a fila que rompa la consistencia agregada.
Decisión

Se adopta para employee.cost_center un modelo de vertical de tipo DISTRIBUTED_TIMELINE, cuyo elemento funcional real no es una fila aislada, sino una ventana de distribución de centros de coste para un empleado.

La vertical:

será historizada;
permitirá múltiples líneas activas simultáneamente;
validará catálogo COST_CENTER;
exigirá contención dentro de una presence;
impondrá que la suma de porcentajes activos no supere 100;
y, cuando exista más de una línea paralela activa, exigirá que todas compartan la misma startDate.

La unidad funcional de cambio será la distribución vigente desde una fecha, no la edición arbitraria de una línea individual.

Definición funcional

employee.cost_center representa la distribución de imputación organizativa de un empleado entre uno o varios centros de coste, con vigencia temporal.

No modela simplemente “una asignación más”, sino el reparto funcional del empleado entre centros de coste para un periodo dado.

Ejemplos válidos:

100% CC_FINANCE
60% CC_IT + 40% CC_SHARED
50% CC_OPS + 50% CC_TRANSFORMATION

Ejemplos inválidos:

80% CC_A + 30% CC_B
50% CC_A desde 01/04 y 50% CC_B desde 15/04, coexistiendo en la misma vigencia
una línea fuera de presence activa
porcentajes 0 o negativos
Tipo de vertical

Clasificación formal:

bounded context: employee
vertical: cost_center
tipo: DISTRIBUTED_TIMELINE
catálogo: SIMPLE
reglas temporales:
MULTI_ACTIVE
SUM_PERCENTAGE_LTE_100
CONTAINED_IN_PRESENCE
PARALLEL_WINDOW_SAME_START_DATE

Esto consolida la vertical en el cluster distribuido del proyecto y evita tratarla como una variación accidental de work_center.

Identidad funcional
Identidad del empleado
ruleSystemCode
employeeTypeCode
employeeNumber
Identidad del recurso

A nivel de API pública de escritura, la unidad funcional relevante será una ventana de distribución identificada por:

empleado
startDate

Es decir, conceptualmente:

ruleSystemCode
employeeTypeCode
employeeNumber
startDate
Nota importante

Internamente, la persistencia puede seguir usando filas individuales con:

id técnico
costCenterAssignmentNumber técnico/funcional interno

pero esos identificadores no definen la identidad pública canónica del recurso.

La razón es funcional: cuando varias líneas comparten una misma distribución vigente, el dominio no las trata como historias autónomas, sino como partes de una única ventana de distribución.

Ventana de distribución

Se introduce el concepto explícito de:

Cost Center Distribution Window

Una ventana de distribución es el conjunto de líneas de cost_center que:

pertenecen al mismo empleado;
comparten la misma startDate;
forman una distribución activa o histórica coherente;
y se validan conjuntamente como una única unidad funcional.

Consecuencias:

crear una distribución = crear una ventana;
sustituir una distribución = cerrar ventana anterior y crear una nueva;
cerrar una distribución = cerrar todas las líneas de la ventana;
TERMINATION actúa sobre la ventana activa completa, no sobre una línea suelta.
Propiedades estructurales
Propiedad	Valor
historized	true
occurrence_type	MULTIPLE
simultaneous_occurrences	MULTIPLE_ACTIVE
lifecycle_strategy	CLOSE
delete_policy	FORBIDDEN
maintenance_style	DISTRIBUTED_WINDOW
Campos funcionales de línea

Cada línea de distribución contiene:

costCenterCode
allocationPercentage
startDate
endDate

Campos enriquecidos de lectura:

costCenterName
isCurrent

Campos derivados de la ventana:

windowStartDate
windowEndDate
totalAllocationPercentage
Validación de catálogo

costCenterCode debe validarse contra rulesystem.rule_entity usando:

ruleEntityTypeCode = COST_CENTER

Esto se alinea con la convención de naming de catálogos reutilizables y con el binding ya previsto para:

employee.cost_center / costCenterCode -> COST_CENTER (DIRECT)

La validación debe comprobar:

ruleSystemCode correcto
existencia del código
activo
vigencia temporal aplicable
Reglas de dominio
1. Contención en presence

Toda línea de cost_center debe estar completamente contenida en una presence válida del empleado.

No se permiten líneas:

antes del inicio de la presence que las contiene;
después del final de la presence;
ni abiertas más allá de una presence cerrada.
2. Multi-actividad permitida

Puede haber múltiples líneas activas simultáneamente para una misma fecha, siempre que pertenezcan a la misma ventana de distribución.

3. Suma máxima de porcentaje

Para cualquier fecha dada, la suma de allocationPercentage de todas las líneas activas del empleado no puede superar 100.

Regla:

permitido: total < 100
permitido: total = 100
prohibido: total > 100
4. Misma fecha de inicio en paralelo

Si existe más de una línea activa simultáneamente dentro de una misma distribución, todas deben compartir exactamente la misma startDate.

Ejemplo válido:

CC_A 50% desde 2026-04-01
CC_B 50% desde 2026-04-01

Ejemplo inválido:

CC_A 50% desde 2026-04-01
CC_B 50% desde 2026-04-15
5. No mezcla incoherente de ventanas activas

No puede coexistir, para una misma fecha, una línea activa perteneciente a una ventana de distribución distinta.

Dicho de otro modo: para un empleado, la distribución activa en una fecha debe ser interpretable como una única ventana funcional.

6. Porcentaje válido por línea

allocationPercentage debe cumplir:

> 0
<= 100
7. Integridad temporal

endDate no puede ser anterior a startDate.

8. Sin edición arbitraria de identidad

No se permite mutar, en una corrección administrativa, los campos que redefinen funcionalmente una línea histórica de forma que rompan la semántica de la ventana.

Operaciones canónicas

No se adopta un CRUD plano por fila.

Las operaciones canónicas del vertical serán orientadas a ventana.

1. Crear distribución

Crea una nueva ventana de distribución desde una fecha.

POST /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers/distributions

Request
startDate
items[]
costCenterCode
allocationPercentage
2. Consultar histórico de distribuciones

GET /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers

3. Consultar distribución vigente

GET /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers/current

4. Sustituir distribución desde fecha

POST /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers/replace-from-date

Request
effectiveDate
items[]
costCenterCode
allocationPercentage

Semántica:

cerrar la ventana activa previa si cubre la fecha;
crear nueva ventana desde effectiveDate;
validar projected timeline distribuida.
5. Cerrar distribución

POST /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/cost-centers/distributions/{startDate}/close

Request
endDate

Semántica:

cerrar todas las líneas de la ventana identificada por startDate.
Operaciones descartadas

Se rechazan como canónicas:

PUT por línea aislada
DELETE físico por línea
edición arbitraria de una fila dentro de una distribución multi-línea activa
endpoints públicos por id
endpoints públicos por costCenterAssignmentNumber

porque esas operaciones empujan el modelo hacia una semántica de filas independientes que no representa bien el dominio.

Relación con replace patterns

employee.cost_center puede necesitar una operación funcional tipo replaceFromDate, pero no debe reutilizar directamente el StrongTimelineReplacePlanner, ya que este patrón está reservado a verticales STRONG_TIMELINE con:

single active
no overlap
full coverage

y cost_center es explícitamente un caso distinto.

Si aparece lógica temporal repetida, podrá introducirse en el futuro un helper ligero específico para distribuciones, por ejemplo:

CostCenterDistributionProjector
DistributedTimelineWindowPlanner

pero no debe crearse todavía un framework genérico ni un motor abstracto.

Relación con TERMINATION

TERMINATION debe cerrar la distribución activa completa del empleado.

Regla funcional:

al terminar un empleado en terminationDate,
se identifican todas las líneas activas de cost_center en esa fecha,
y todas deben quedar cerradas con esa fecha de fin.

Consecuencias:

no puede quedar ninguna línea abierta después de la terminación;
no se redistribuyen porcentajes;
no se corrigen líneas;
no se fuerza una suma distinta;
simplemente se cierra la ventana activa.

Si no existe distribución activa en la fecha de terminación, no se considera error funcional por sí mismo.

Esto encaja con la semántica general de lifecycle workflows y con la necesidad de cerrar asignaciones vigentes sin dejar residuos abiertos.

Relación con Journey

En Journey / timeline, un cambio de cost_center debe interpretarse como evento funcional por ventana de distribución, no como cascada de eventos aislados por cada fila técnica.

Evento esperado:

COST_CENTER_CHANGE

La interpretación debe hacerse en backend y no delegarse al frontend.

Persistencia

Persistencia recomendada: tabla por líneas.

Ejemplo conceptual:

employee.cost_center

Campos:

id
employee_id
cost_center_assignment_number
cost_center_code
allocation_percentage
start_date
end_date
created_at
updated_at
Restricciones mínimas de base de datos
PK técnica por id
unique (employee_id, cost_center_assignment_number)
check allocation_percentage > 0 and allocation_percentage <= 100
check end_date is null or start_date < end_date
Nota

Las reglas:

suma <= 100
misma startDate en multi-activo
una única ventana funcional activa por fecha

deben validarse en dominio / servicio de aplicación, no intentarse imponer únicamente con constraints SQL.

API y OpenAPI

La API pública debe seguir las reglas generales del proyecto:

business keys del empleado
sin IDs técnicos
sin mezclar parent por business key e hijo por id técnico

La OpenAPI debe reflejar:

operaciones por ventana
requests con items[]
DTOs claros y honestos
sin introducir update DTOs que permitan mutar identidad funcional como si fuera CRUD genérico.
Read models recomendados

La lectura debe exponer labels enriquecidas:

costCenterCode
costCenterName

y agrupar claramente la ventana actual e histórico.

Ejemplo conceptual de respuesta:

{
  "employee": {
    "ruleSystemCode": "ESP",
    "employeeTypeCode": "EMP",
    "employeeNumber": "000123"
  },
  "currentDistribution": {
    "startDate": "2026-04-01",
    "endDate": null,
    "totalAllocationPercentage": 100,
    "items": [
      {
        "costCenterCode": "CC_A",
        "costCenterName": "Administración",
        "allocationPercentage": 50
      },
      {
        "costCenterCode": "CC_B",
        "costCenterName": "Transformación",
        "allocationPercentage": 50
      }
    ]
  }
}

Esto se alinea con la estrategia general del proyecto de enriquecer lectura con code + name y no obligar al frontend a reconstruir semántica desde catálogos masivos.

Frontend

Esta vertical no debe tratarse como:

SLOT
ni tabla CRUD genérica

La semántica de UI recomendada es una sección temporal/distribuida con:

distribución actual destacada
histórico de distribuciones
acciones honestas:
Añadir distribución
Sustituir distribución desde fecha
Cerrar distribución

No deben usarse como acciones primarias:

editar fila
borrar fila
update técnico
grid CRUD

Esto es coherente con el principio general de UX honesta y con la prohibición de usar “Editar” como verbo universal cuando la semántica real es otra.

Consecuencias positivas
mejor alineación con el dominio real;
evita modelado accidental por filas;
simplifica TERMINATION;
facilita Journey semántico;
hace más clara la UI;
prepara futuras abstracciones de distributed timeline;
mantiene consistencia con el mapa y lenguaje ya definidos en B4RRHH.
Costes / riesgos
requiere lógica de validación agregada, no trivial;
introduce una noción nueva de ventana funcional;
obliga a resistir la tentación de implementar CRUD simple por línea;
puede requerir helper técnico específico en el futuro si aparecen más verticales distribuidas;
la corrección administrativa de histórico deberá definirse con cuidado si algún día se habilita.
Alternativas consideradas
1. Modelarlo como clon de work_center

Descartado.

work_center es una asignación flexible historizada, pero cost_center tiene una semántica distribuida basada en porcentaje y paralelismo.

2. Modelarlo como CRUD por fila

Descartado.

Rompe la unidad funcional de distribución, genera estados incoherentes y hace más difícil validar suma, ventanas y termination.

3. Tratarlo como STRONG_TIMELINE

Descartado.

No es single-active y el planner de strong timeline no aplica directamente.

Resumen

employee.cost_center se define en B4RRHH como una vertical DISTRIBUTED_TIMELINE que modela la distribución temporal del empleado entre uno o varios centros de coste.

La unidad funcional de mantenimiento no será una fila aislada, sino una ventana de distribución identificada por empleado + startDate.

Reglas clave:

multi-activo permitido;
suma activa <= 100;
contención en presence;
líneas paralelas con la misma startDate;
cierre completo en TERMINATION;
catálogo COST_CENTER;
operaciones canónicas orientadas a crear, sustituir y cerrar distribuciones