ADR — Payroll Launch, Calculation Run, Claim and Internal Calculator Orchestration
Estado

Propuesto

Contexto

El bounded context payroll ya ha fijado una base importante:

la raíz funcional es payroll.payroll;
su identidad funcional es:
ruleSystemCode
employeeTypeCode
employeeNumber
payrollPeriodCode
payrollTypeCode
presenceNumber;
payroll.payroll representa un resultado materializado de cálculo, no un CRUD editable;
sus hijos (payroll_concept, payroll_context_snapshot) dependen completamente de la raíz y deben eliminarse por cascade;
los estados de nómina gobiernan si una nómina puede o no ser sustituida;
una nómina existente solo es recalculable si está en NOT_VALID;
una unidad sin nómina previa también debe ser elegible para cálculo inicial.

También se ha decidido ya que el endpoint actual POST /payrolls/calculate no representa el futuro motor real, sino un stub temporal de validación de pipeline, donde el cliente aún aporta conceptos y snapshots explícitamente para poder probar el flujo de extremo a extremo antes de diseñar el lanzador real y el calculador definitivo.

En paralelo, B4RRHH tiene reglas de arquitectura muy claras:

primero se organiza por vertical/subdominio y dentro de cada vertical se aplica arquitectura hexagonal;
las APIs públicas deben usar business keys, no IDs técnicos;
cuando una operación no encaja como CRUD plano, debe modelarse como workflow/caso de uso explícito y no como recurso falso o tabla oportunista.

Al hablar del lanzamiento de nómina aparecen dos problemas de diseño que no deben mezclarse:

qué significa lanzar un cálculo;
cómo evitar que dos lanzamientos simultáneos procesen la misma unidad de cálculo.

Además, si el lanzamiento solo devuelve un body HTTP efímero, se pierde una capacidad que será útil muy pronto:

consultar desde frontend cómo va un cálculo;
saber cuántas unidades se han procesado;
ver qué se ha omitido, qué se ha reclamado, qué terminó en CALCULATED, qué quedó en NOT_VALID y qué falló.

Por todo ello, hace falta un modelo explícito para:

el workflow de launch;
la persistencia de los runs;
la exclusión concurrente por unidad de cálculo;
el desacoplamiento del calculador real respecto del endpoint público.
Problema

Se necesita definir una arquitectura de lanzamiento de nómina que:

permita lanzar cálculos sobre una población objetivo;
resuelva y expanda dicha población a unidades reales de cálculo;
filtre elegibilidad sin recalcular resultados protegidos;
permita concurrencia segura;
deje preparada la paralelización futura;
permita seguimiento de progreso;
desacople el launch del motor de cálculo real;
evite convertir el cálculo actual stub en contrato definitivo por accidente.
Decisión

Se adopta una arquitectura de orquestación de cálculo basada en cuatro piezas distintas:

payroll.payroll
Resultado materializado de una unidad de cálculo.
payroll.calculation_run
Recurso técnico-operativo persistido que representa una ejecución de lanzamiento.
payroll.calculation_claim
Recurso técnico de exclusión concurrente por unidad de cálculo.
calculate como caso de uso interno especializado, no como contrato público canónico del motor definitivo.

El lanzamiento de nómina se modela como un workflow explícito que:

crea un calculation_run;
resuelve población objetivo;
expande a unidades de cálculo;
determina elegibilidad;
intenta adquirir claims por unidad;
delega el cálculo efectivo a un calculador interno;
registra progreso y resumen.
Principio madre

Launch no calcula; launch coordina.
Calculate no decide población; calculate materializa una unidad.

Esta separación es obligatoria.

Definiciones principales
1. Unidad de cálculo

La unidad mínima de cálculo es:

ruleSystemCode
employeeTypeCode
employeeNumber
payrollPeriodCode
payrollTypeCode
presenceNumber

Esta unidad es coherente con la business key de payroll.payroll ya adoptada y con el hecho de que una presencia concreta representa una relación laboral concreta del empleado.

Regla

El launch siempre trabaja con una colección de unidades de cálculo, no con empleados abstractos.

2. Population target vs eligible units

Se distinguen dos conceptos.

Población objetivo

Es el conjunto de empleados o ámbitos que el usuario quiere lanzar.

Ejemplos:

un empleado concreto;
una lista explícita de empleados;
todos los empleados de un scope determinado.
Unidades elegibles

Son las unidades de cálculo que realmente pueden entrar al cálculo.

Una unidad es elegible si:

no existe payroll.payroll previa para su business key, o
existe y su estado actual es NOT_VALID.

Una unidad no es elegible si existe y está en:

CALCULATED
EXPLICIT_VALIDATED
DEFINITIVE

Esta regla mantiene el guardarraíl funcional ya fijado para payroll y evita recálculos accidentales sobre resultados vigentes o protegidos.

3. payroll.calculation_run

Se introduce un recurso técnico-operativo persistido:

payroll.calculation_run
Naturaleza

No es una nómina.
No es un resultado de negocio final.
No sustituye a payroll.payroll.

Representa una ejecución de lanzamiento.

Objetivo

Permitir:

seguimiento del progreso;
trazabilidad del lanzamiento;
resumen persistido;
futura consulta desde frontend;
base para asincronía o paralelización posterior.
Campos mínimos recomendados
id técnico interno
ruleSystemCode
payrollPeriodCode
payrollTypeCode
calculationEngineCode
calculationEngineVersion
requestedAt
requestedBy nullable
status
targetSelectionJson
campos agregados de resumen o summaryJson
Estados recomendados del run
REQUESTED
RUNNING
COMPLETED
COMPLETED_WITH_ERRORS
FAILED
Regla

La máquina de estados de calculation_run es independiente de la máquina de estados de payroll.payroll.

No deben mezclarse.

4. payroll.calculation_claim

Se introduce un recurso técnico de exclusión concurrente:

payroll.calculation_claim
Naturaleza

No representa negocio visible al usuario final.
No sustituye a locks de BD del aggregate.
No es un recurso funcional público.

Su misión es impedir que dos runs distintos procesen simultáneamente la misma unidad de cálculo.

Claim key

La identidad funcional del claim es exactamente la unidad de cálculo:

ruleSystemCode
employeeTypeCode
employeeNumber
payrollPeriodCode
payrollTypeCode
presenceNumber
Campos recomendados
id técnico
claim key completa
runId
claimedAt
claimedBy nullable
Restricción obligatoria

Debe existir una restricción única por claim key completa.

Regla de adquisición

La adquisición del claim debe ser atómica.

No se recomienda una lógica de:

leer si existe
y luego insertar

por riesgo de carrera.

La lógica correcta es:

intentar insertar claim;
si inserta, la unidad queda reclamada por ese run;
si falla por unicidad, esa unidad ya está siendo procesada por otro run y debe ignorarse o marcarse como no reclamada.
Regla de limpieza

Al finalizar el procesamiento de la unidad, el claim se elimina.

Extensión futura

Más adelante puede añadirse:

expiresAt
recuperación de claims huérfanos
housekeeping periódico

pero no es requisito de esta decisión base.

5. payroll.calculation_run_item

Se recomienda introducir, ya desde la base o en una fase muy cercana, una tabla hija de seguimiento fino:

payroll.calculation_run_item
Naturaleza

Representa el estado de una unidad concreta dentro de un run concreto.

Objetivo

Permitir:

trazabilidad por unidad;
saber qué pasó con cada cálculo;
alimentar frontend con progreso real;
distinguir skip, claim conflict, calculated, not valid, error, etc.
Campos recomendados
id
runId
calculation key completa
status
reasonCode nullable
processedAt nullable
message nullable opcional
Estados sugeridos
CANDIDATE
NOT_ELIGIBLE
CLAIMED
SKIPPED_ALREADY_CLAIMED
CALCULATED
NOT_VALID
ERROR
Regla

calculation_run_item pertenece al seguimiento del run, no al dominio raíz de payroll result.

Semántica de Launch
Definición

Lanzar nómina significa:

crear una ejecución persistida de cálculo, resolver una población objetivo, expandirla a unidades de cálculo, filtrar unidades elegibles, intentar reclamar cada unidad de forma exclusiva, delegar el cálculo efectivo al calculador interno y registrar el progreso y resultado del proceso.

Responsabilidades obligatorias del launch

El launch debe:

crear calculation_run;
validar contexto de ejecución;
resolver la población objetivo;
expandirla a unidades de cálculo;
determinar elegibilidad;
crear run_items o equivalente lógico;
intentar adquirir claim por unidad elegible;
delegar en calculate interno;
consolidar estados por unidad;
actualizar resumen y estado final del run.

El launch no debe:

generar conceptos él mismo;
implementar reglas salariales;
convertirse en motor de cálculo;
depender de HTTP interno al propio backend si launch y calculate viven en el mismo servicio.
Input mínimo del launch

Se recomienda que el launch reciba al menos:

ruleSystemCode
payrollPeriodCode
payrollTypeCode
calculationEngineCode
calculationEngineVersion
targetSelection
targetSelection

Debe permitir al menos:

empleado concreto
lista explícita
ámbito masivo simple

El shape contractual exacto podrá evolucionar, pero el launch debe conservar esta responsabilidad de resolución.

Output del launch

El launch no debe limitarse a devolver “201 created” con un resumen efímero.

Debe devolver, al menos:

runId
estado inicial o final del run
resumen agregado

y permitir después consultar el run persistido.

Calculate interno
Decisión clave

El futuro calculate no debe consolidarse como endpoint público canónico.

El endpoint actual de calculate se acepta solo como stub temporal de validación de pipeline, tal como ya está documentado en OpenAPI.

La decisión de fondo es:

el cálculo serio será largo, cambiante y costoso de desarrollar;
por tanto, el launch no debe acoplarse a un endpoint público rígido del motor.
Regla arquitectónica

El launch debe invocar un caso de uso interno de cálculo, no un endpoint HTTP del propio backend.

Ejemplo conceptual:

CalculatePayrollUnitUseCase
Responsabilidad del calculate interno

Recibir una unidad explícita y materializar un resultado:

creando payroll.payroll si no existe;
sustituyéndola si existe y es recalculable;
generando CALCULATED o NOT_VALID según corresponda.
Importante

El calculate interno no resuelve poblaciones.
Eso pertenece exclusivamente al launch.

Concurrencia
Decisión principal

La concurrencia se gobierna mediante payroll.calculation_claim, no mediante el aggregate payroll.payroll.

Justificación

La concurrencia aquí es un problema del workflow de ejecución, no de la identidad del recurso raíz.

Reglas
dos runs pueden existir simultáneamente;
dos runs no pueden procesar simultáneamente la misma unidad de cálculo;
si una unidad ya está reclamada por otro run, el launch actual debe marcarla como no reclamada / ya en curso y seguir adelante.
Diseño objetivo vs implementación inicial
Diseño objetivo

Exclusión por unidad de cálculo.

Implementación inicial recomendada

La propia claim table ya permite ese diseño desde la primera iteración, por lo que no se considera necesario arrancar con un bloqueo global de launch.

Consecuencia

La paralelización futura queda abierta desde el primer día, aunque inicialmente el procesamiento interno pueda seguir siendo secuencial.

Relación entre claim y business key de payroll

La restricción única de payroll.payroll por business key sigue siendo obligatoria y valiosa, pero no se considera mecanismo principal de coordinación concurrente.

Papel de la unique en payroll
protege integridad final del resultado;
actúa como última línea de defensa.
Papel del claim
evita que dos runs intenten procesar simultáneamente la misma unidad.

Por tanto:

la unique de payroll no sustituye a la claim table;
la claim table no sustituye a la unique del root.

Ambas son necesarias y cumplen papeles distintos.

Procesamiento secuencial vs paralelo
Regla base

El ADR no obliga a que el launch sea síncrono o asíncrono, ni a que procese secuencial o paralelamente.

Lo que sí fija es la semántica.

V1 aceptable
run persistido
claims por unidad
procesamiento secuencial dentro del launch
resumen final persistido
Evolución natural
paralelización por chunks o workers
asíncrono
polling desde frontend del estado del run
reintentos por unidad

El diseño aquí debe soportar esas evoluciones sin rehacer la semántica.

API pública recomendada
Endpoints canónicos de resultado

Se mantienen por business key:

GET payroll by business key
invalidate
explicit-validate
finalize

Esto sigue la convención general del proyecto de usar business keys públicas y acciones explícitas cuando el dominio lo pide.

Endpoint de launch

Se recomienda un endpoint público explícito de negocio, por ejemplo:

POST /payroll/calculation-runs/launch

o naming equivalente claramente orientado a ejecución.

Lectura de run

Se recomienda poder consultar:

GET /payroll/calculation-runs/{runId}

y eventualmente listar runs recientes o items asociados.

Calculate

El endpoint actual de calculate no se considera canónico a futuro. Su continuidad se limita a la fase stub/pre-launch ya documentada.

Qué se rechaza explícitamente

Se rechaza:

tratar launch como CRUD;
hacer que launch invoque HTTP contra su propio backend como arquitectura permanente;
bloquear necesariamente todo el sistema a un solo launch global;
usar solo la unique de payroll como solución de concurrencia;
mezclar calculation_run con payroll.payroll;
convertir calculation_run en una nueva raíz funcional de negocio;
fijar ya el contrato definitivo del motor de cálculo real;
acoplar la semántica de launch al stub actual de calculate.
Consecuencias
Positivas
separación limpia entre resultado, ejecución, concurrencia y motor;
base sólida para meses de evolución sin rehacer el modelo;
posibilidad de seguimiento de runs desde frontend;
paralelización futura preparada desde el diseño;
acoplamiento bajo entre launch y motor real;
protección real frente a colisiones concurrentes.
Costes
introduce recursos técnicos adicionales (calculation_run, calculation_claim, probablemente calculation_run_item);
exige disciplina para no mezclar estados de run con estados de payroll;
añade trabajo de persistencia y de resumen/progreso.
Plan recomendado por fases
Fase 1
introducir calculation_run
introducir calculation_claim
introducir launch
mantener procesamiento secuencial
calculate sigue siendo interno
persistir resumen básico del run
Fase 2
introducir calculation_run_item
seguimiento fino por unidad
consulta desde frontend del progreso
Fase 3
paralelización real
workers o executor
asincronía y polling más rico
posible housekeeping de claims
Resumen ejecutivo

En B4RRHH, el lanzamiento de nómina no se modelará como un simple POST que calcula y devuelve un body efímero.

Se adopta una arquitectura en la que:

payroll.payroll sigue siendo el resultado materializado por unidad;
payroll.calculation_run representa una ejecución persistida de lanzamiento;
payroll.calculation_claim garantiza exclusión concurrente por unidad de cálculo;
calculate será un caso de uso interno especializado y desacoplado del contrato público final;
el launch coordina, reclama, delega y registra;
la semántica queda preparada tanto para una V1 secuencial como para una evolución futura paralelizable y observable.