ADR — Modelo físico de payroll launch, calculation run, claims y mensajes
Estado

Propuesto

Contexto

El bounded context payroll ya tiene fijadas varias decisiones estructurales:

payroll.payroll es la raíz funcional del resultado materializado de nómina;
su business key es:
ruleSystemCode
employeeTypeCode
employeeNumber
payrollPeriodCode
payrollTypeCode
presenceNumber;
payroll.payroll no es un CRUD editable, sino un resultado de cálculo;
los hijos payroll_concept y payroll_context_snapshot dependen completamente de la raíz y se eliminan por cascade;
el estado de la nómina (NOT_VALID, CALCULATED, EXPLICIT_VALIDATED, DEFINITIVE) vive en la propia payroll y gobierna si puede ser sustituida o no;
una unidad sin nómina previa también debe ser elegible para cálculo inicial.

También se ha consolidado otra separación importante:

launch coordina;
calculate materializa una unidad;
la concurrencia no debe gobernarse dentro del aggregate payroll.payroll, sino en una capa técnica de ejecución;
el proyecto prefiere workflows explícitos cuando una operación no encaja como CRUD plano.

Durante el diseño del lanzamiento apareció una discusión relevante sobre el detalle por unidad de ejecución.

Se descarta como núcleo inicial una tabla obligatoria de calculation_run_item porque:

generaría una fila por unidad para cada run;
puede producir mucho volumen con poco valor persistente;
muchos errores funcionales pertenecen realmente a la nómina materializada y no al run;
tras un nuevo cálculo de esa unidad, gran parte de ese detalle pierde relevancia operativa.

En cambio, sí se considera útil distinguir dos tipos de mensajes:

mensajes adheridos a la nómina
pertenecen al resultado materializado y deben vivir como vertical hija de payroll.payroll;
mensajes del run
pertenecen a la ejecución técnica del launch y pueden existir incluso cuando no se materializa una nueva payroll.
Problema

Se necesita un modelo físico que permita:

persistir ejecuciones de launch;
seguir su progreso general;
impedir concurrencia simultánea sobre la misma unidad de cálculo;
registrar mensajes operativos/técnicos del run;
registrar mensajes funcionales o revisables adheridos a una payroll;
mantener separado:
el resultado materializado (payroll.payroll)
de la ejecución técnica (calculation_run, calculation_claim, calculation_run_message).

El modelo debe ser suficiente para meses de evolución, sin fijar todavía el motor real de reglas.

Decisión

Se adopta dentro del schema payroll el siguiente modelo físico base:

payroll.payroll
resultado materializado de una unidad de cálculo. Ya existente.
payroll.payroll_warning
mensajes funcionales adheridos a una nómina concreta.
payroll.calculation_run
ejecución técnica persistida de un launch.
payroll.calculation_claim
exclusión concurrente por unidad de cálculo.
payroll.calculation_run_message
mensajes operativos, técnicos o de exclusión del propio run.

Se decide no introducir payroll.calculation_run_item como tabla obligatoria en la base inicial.

Principio estructural

La payroll persiste resultado.
La payroll_warning persiste mensajes funcionales de ese resultado.
El run persiste la ejecución.
El claim persiste exclusión concurrente.
El run_message persiste incidencias y mensajes de la ejecución.

Cada pieza resuelve un problema distinto.

1. Tabla payroll.calculation_run
Propósito

Representar una ejecución técnica de lanzamiento de nómina.

No es una payroll.
No es una raíz funcional de negocio.
No sustituye a payroll.payroll.

Sirve para:

trazabilidad operativa;
seguimiento desde backend y frontend;
resumen persistido del lanzamiento;
futura asincronía o paralelización.
Columnas propuestas
id bigint generated always as identity primary key
rule_system_code varchar(5) not null
payroll_period_code varchar(30) not null
payroll_type_code varchar(30) not null
calculation_engine_code varchar(50) not null
calculation_engine_version varchar(50) not null
requested_at timestamp not null
requested_by varchar(100) null
status varchar(30) not null
target_selection_json json not null
total_candidates integer not null default 0
total_eligible integer not null default 0
total_claimed integer not null default 0
total_skipped_not_eligible integer not null default 0
total_skipped_already_claimed integer not null default 0
total_calculated integer not null default 0
total_not_valid integer not null default 0
total_errors integer not null default 0
started_at timestamp null
finished_at timestamp null
summary_json json null
created_at timestamp not null default now()
updated_at timestamp not null default now()
Justificación
Contexto del run
rule_system_code
payroll_period_code
payroll_type_code
calculation_engine_code
calculation_engine_version

definen el marco operativo del lanzamiento.

target_selection_json

Se persiste en JSON porque representa la selección objetivo del launch y todavía no compensa fijar un modelo relacional complejo para todas sus variantes.

Contadores agregados

Se mantienen como columnas explícitas porque permiten:

seguimiento rápido;
respuesta de UI;
observabilidad del run;
resumen estable sin depender de una tabla hija por unidad.
summary_json

Se admite para detalles flexibles adicionales, sin forzar migraciones por cada refinamiento menor del resumen.

Estados recomendados del run
REQUESTED
RUNNING
COMPLETED
COMPLETED_WITH_ERRORS
FAILED
Restricciones recomendadas
checks de no negatividad en contadores
check (finished_at is null or started_at is not null)
Índices recomendados
(rule_system_code, payroll_period_code, payroll_type_code)
(status)
(requested_at desc)
2. Tabla payroll.calculation_claim
Propósito

Persistir la exclusión concurrente por unidad de cálculo.

Su misión es impedir que dos runs distintos procesen al mismo tiempo la misma unidad:

ruleSystemCode
employeeTypeCode
employeeNumber
payrollPeriodCode
payrollTypeCode
presenceNumber
Columnas propuestas
id bigint generated always as identity primary key
run_id bigint not null
rule_system_code varchar(5) not null
employee_type_code varchar(30) not null
employee_number varchar(15) not null
payroll_period_code varchar(30) not null
payroll_type_code varchar(30) not null
presence_number integer not null
claimed_at timestamp not null
claimed_by varchar(100) null
FK recomendada
fk_calculation_claim_run
run_id -> payroll.calculation_run(id)
on delete cascade
Restricción clave

Debe existir una unique fuerte por la calculation key completa:

(rule_system_code, employee_type_code, employee_number, payroll_period_code, payroll_type_code, presence_number)
Regla de adquisición

La adquisición del claim debe ser atómica mediante insert.

No se acepta como patrón base:

leer si existe;
luego insertar.
Semántica de vida
si el insert entra, la unidad queda reclamada;
si falla por unique, la unidad ya está en curso en otro run;
al terminar de procesar la unidad, el claim se elimina.
Por qué no guardar status en claim

Porque claim no es una mini máquina de estados.
Su única misión es representar posesión exclusiva temporal de una unidad.

Índice recomendado
(run_id)
3. Tabla payroll.payroll_warning
Propósito

Persistir mensajes funcionales adheridos a una nómina concreta.

No representan incidencias del run, sino mensajes del resultado materializado.

Pueden incluir:

errores funcionales;
avisos;
observaciones;
cosas a revisar por usuario;
mensajes no bloqueantes pero relevantes.
Naturaleza semántica

Se adopta el término warning de forma deliberada para no encerrar la semántica en “error”.

El diseño debe permitir:

payroll NOT_VALID con warnings de severidad ERROR;
payroll CALCULATED con warnings de severidad WARNING;
payroll con mensajes informativos futuros.
Columnas propuestas
id bigint generated always as identity primary key
payroll_id bigint not null
warning_code varchar(50) not null
severity_code varchar(20) not null
message varchar(500) not null
details_json json null
FK recomendada
fk_payroll_warning_payroll
payroll_id -> payroll.payroll(id)
on delete cascade
Restricción única recomendada

No fijaría una unique demasiado agresiva de entrada.

Podría existir más de un warning con el mismo warning_code si en el futuro aparece necesidad de varias ocurrencias contextualizadas.
Si se quiere una deduplicación ligera, preferiría resolverla en dominio antes que forzarla ya en esquema.

Por qué no tiene created_at

Se decide explícitamente no añadir created_at.

Justificación:

payroll_warning nace y muere con la payroll.payroll;
el instante relevante ya está representado por payroll.calculated_at;
añadir otro timestamp duplicaría semántica sin aportar valor real.
Severidades recomendadas
INFO
WARNING
ERROR
Índices recomendados
(payroll_id)
opcionalmente (severity_code) si más adelante se consulta mucho por severidad
4. Tabla payroll.calculation_run_message
Propósito

Persistir mensajes del propio run.

Representa:

incidencias operativas;
errores técnicos;
descartes por claim;
descartes por no elegibilidad;
mensajes de ejecución no adheribles a una payroll concreta.

Ejemplos:

“unidad descartada por claim activo”
“unidad omitida por estado EXPLICIT_VALIDATED”
“error técnico en acceso a BD”
“fallo al resolver población”
“run completado con conflictos parciales”
Regla semántica

calculation_run_message no reemplaza a payroll_warning.

payroll_warning

mensaje funcional del resultado de nómina

calculation_run_message

mensaje operativo/técnico/de ejecución del run

Columnas propuestas
id bigint generated always as identity primary key
run_id bigint not null
message_code varchar(50) not null
severity_code varchar(20) not null
message varchar(500) not null
details_json json null
rule_system_code varchar(5) null
employee_type_code varchar(30) null
employee_number varchar(15) null
payroll_period_code varchar(30) null
payroll_type_code varchar(30) null
presence_number integer null
created_at timestamp not null default now()
FK recomendada
fk_calculation_run_message_run
run_id -> payroll.calculation_run(id)
on delete cascade
Justificación de la calculation key nullable

Se permite asociar un mensaje del run a:

una ejecución global;
o a una unidad concreta dentro del run.

Por eso la calculation key es nullable:

si el mensaje es global, queda vacía;
si el mensaje se refiere a una unidad concreta, se rellena.

Esto evita la necesidad de una tabla run_item obligatoria por cada unidad.

Severidades recomendadas
INFO
WARNING
ERROR
Índices recomendados
(run_id)
(run_id, severity_code)
opcionalmente (run_id, employee_type_code, employee_number) si más adelante se necesita drill-down por empleado
5. Relación con payroll.payroll
Regla estructural

payroll.payroll permanece como resultado materializado y no absorbe campos de launch, run, claim ni mensajes operativos.

No se deben añadir a payroll.payroll cosas como:

estado del run;
claim status;
resumen de ejecución;
mensajes técnicos del launch.
Lo que sí absorbe

Sí absorbe:

su estado funcional (status);
su razón (statusReasonCode);
y sus payroll_warning.

Esto mantiene coherente la separación entre:

resultado de negocio materializado
ejecución técnica que lo produjo
6. Decisión explícita sobre calculation_run_item
Decisión

Se decide no introducir payroll.calculation_run_item como tabla base obligatoria.

Justificación
1. Volumen

Generaría una fila por unidad y por run, con mucho crecimiento potencial para poco valor si la mayoría de unidades se comportan normalmente.

2. Relevancia temporal

Una vez existe un nuevo run sobre la misma unidad, buena parte del detalle fino del item anterior pierde valor operativo.

3. Errores funcionales

Los errores funcionales importantes pertenecen a la nómina materializada y deben vivir en payroll.payroll mediante payroll_warning, no en el run.

4. Observabilidad suficiente para V1

La combinación de:

calculation_run
calculation_claim
calculation_run_message
payroll_warning

proporciona una observabilidad suficientemente rica sin necesidad de una tabla hija obligatoria por unidad.

Evolución futura posible

No se prohíbe introducir calculation_run_item más adelante si la observabilidad operativa futura lo justifica.

Pero no forma parte del núcleo inicial.

7. JSON vs relacional
JSON permitido

Se admite JSON en:

target_selection_json
summary_json
details_json de warnings
details_json de run messages

porque ahí la variación todavía no compensa fijarla toda en columnas.

Relacional obligatorio

Se exige modelado relacional explícito en:

calculation key del claim
contexto base del run
estado del run
referencias payroll/run
contadores agregados

porque esas piezas sí son núcleo estable del diseño.

8. Restricciones e índices recomendados completos
calculation_run
pk (id)
índices:
(rule_system_code, payroll_period_code, payroll_type_code)
(status)
(requested_at desc)
calculation_claim
pk (id)
fk run_id -> calculation_run(id) on delete cascade
unique:
calculation key completa
índice:
(run_id)
payroll_warning
pk (id)
fk payroll_id -> payroll(id) on delete cascade
índice:
(payroll_id)
calculation_run_message
pk (id)
fk run_id -> calculation_run(id) on delete cascade
índices:
(run_id)
(run_id, severity_code)
9. Qué se rechaza explícitamente

Se rechaza en este modelo físico:

usar solo la unique de payroll.payroll como solución de concurrencia;
meter mensajes técnicos del run dentro de payroll.payroll;
convertir calculation_claim en una tabla de workflow compleja;
introducir calculation_run_item por inercia sin haber demostrado valor real;
fijar ya el contrato definitivo del motor de cálculo;
acoplar launch al endpoint stub actual de calculate, que sigue siendo temporal y no canónico
10. Consecuencias
Positivas
separación muy limpia entre resultado, mensajes funcionales, ejecución y concurrencia;
menos volumen estructural que con una tabla obligatoria de run items;
observabilidad suficiente para V1;
posibilidad de seguimiento desde frontend;
motor real desacoplado del workflow;
base sólida para meses de evolución.
Costes
añade cuatro tablas nuevas respecto al payroll root original;
obliga a distinguir bien mensajes funcionales vs mensajes de run;
deja para una fase futura el drill-down total por unidad si algún día se necesita.
11. Estrategia de implementación recomendada
Fase 1
crear calculation_run
crear calculation_claim
crear payroll_warning
crear calculation_run_message
Fase 2
implementar launch síncrono
persistir run + summary + messages
mantener calculate como caso de uso interno
Fase 3
exponer lectura de runs y mensajes
permitir seguimiento desde frontend
Fase 4
revaluar si la observabilidad futura justifica calculation_run_item
añadir housekeeping de claims si hace falta
introducir paralelización real
12. Resumen ejecutivo

Se adopta para payroll un modelo físico donde:

payroll.payroll sigue siendo el resultado materializado;
payroll.payroll_warning concentra mensajes funcionales adheridos a la nómina;
payroll.calculation_run representa el launch persistido;
payroll.calculation_claim garantiza exclusión concurrente por unidad;
payroll.calculation_run_message concentra mensajes operativos/técnicos del run;
calculation_run_item no forma parte del núcleo inicial;
el sistema queda preparado para diseñar launch sin acoplarlo prematuramente al motor real.