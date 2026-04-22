ADR-043 — Agreement Profile y Activación de Payroll basada en Contexto
Estado

Propuesto

Contexto

El sistema B4RRHH actualmente modela el convenio colectivo (AGREEMENT) como una entidad de catálogo dentro de rule_entity, junto con su relación con categorías (AGREEMENT_CATEGORY).

Este modelo es suficiente para validaciones básicas, pero insuficiente para:

representar información real de negocio del convenio (código oficial, jornada anual, etc.)
alimentar lógica derivada (ej. cálculo de jornada del empleado)
servir como contexto de configuración para el motor de nómina

En paralelo, el motor de nómina (payroll) se está diseñando en torno a un metamodelo de objetos configurables (PayrollObject), donde:

los conceptos (PAYROLL_CONCEPT) representan cálculos
las tablas (TABLE) representan fuentes de datos parametrizadas
las constantes (CONSTANT) representan valores fijos

Además, se identifica la necesidad de que distintos contextos de negocio (rule system, convenio, empresa, etc.):

activen conceptos de nómina aplicables
vinculen fuentes de datos (tablas, constantes)

Finalmente, se detecta una deuda técnica en employee.working_time, donde las horas anuales se encuentran fijadas de forma estática (ej. 2000 horas), cuando en realidad dependen del convenio aplicable.

Problema

Se necesita:

Enriquecer el convenio sin romper el modelo existente basado en rule_entity
Permitir que el convenio participe en la configuración efectiva del cálculo de nómina
Introducir un mecanismo genérico y escalable para:
activar conceptos de nómina
vincular fuentes de datos (tablas)
Definir una estructura eficiente para almacenar datos parametrizados de tablas (ej. salario base por categoría)
Resolver la dependencia entre convenio y jornada laboral del empleado
Evitar:
proliferación de tablas específicas por tipo de entidad (agreement_triggers, etc.)
sobreabstracción prematura del motor de nómina
Decisión
1. Mantener AGREEMENT como catálogo base
AGREEMENT permanece como rule_entity
No se modifica su identidad funcional
El código funcional del convenio será, preferentemente, el código oficial real

Ejemplo:

ruleSystemCode = ESP
agreementCode = 99002405011982
2. Introducir agreement_profile como enriquecimiento

Se crea una nueva entidad:

agreement_profile

Identidad funcional
(ruleSystemCode, agreementCode)
Campos principales
officialAgreementNumber
displayName
shortName
annualHours
active
createdAt
updatedAt
Propósito
Enriquecer el convenio con datos de negocio
Servir como fuente para lógica derivada (ej. jornada del empleado)
3. Derivar la jornada del empleado desde el convenio

Se establece la regla:

employee.working_time no contiene una constante fija de horas anuales;
las horas se derivan del agreement_profile vigente en la fecha de aplicación.

Flujo:

Cambio en working_time
Resolución de convenio/categoría vigente
Lectura de annualHours desde agreement_profile
Cálculo y persistencia de valores derivados
4. Introducir activación de objetos de nómina por contexto

Se crea una tabla genérica:

payroll_object_activation

Campos
ruleSystemCode
ownerTypeCode (RULE_SYSTEM, AGREEMENT, COMPANY, etc.)
ownerCode
targetObjectTypeCode
targetObjectCode
active
Propósito

Permitir que un contexto de negocio active conceptos de nómina.

Restricción V1

Solo se permite:

targetObjectTypeCode = PAYROLL_CONCEPT
Ejemplo
AGREEMENT 99002405011982 → PAYROLL_CONCEPT SALARIO_BASE
5. Introducir binding de objetos de nómina por contexto

Se crea una segunda tabla genérica:

payroll_object_binding

Campos
ruleSystemCode
ownerTypeCode
ownerCode
bindingRoleCode
boundObjectTypeCode
boundObjectCode
active
Propósito

Permitir que un contexto vincule fuentes de datos a roles funcionales.

Ejemplo
AGREEMENT 99002405011982 → BASE_SALARY_TABLE → TABLE SB_RETAIL
Nota

bindingRoleCode es obligatorio para distinguir semántica.

6. Mantener separación semántica: activation vs binding

Se decide explícitamente:

activation ≠ binding
No se utiliza una única tabla genérica para ambos conceptos

Motivo:

semántica distinta
validación distinta
mantenimiento más claro
7. Mantener precedencia fuera de datos (V1)

La precedencia entre contextos:

Ejemplo:

RULE_SYSTEM > AGREEMENT > COMPANY > WORK_CENTER

No se modela en base de datos en V1.

Se define como:

política del motor
fijada en código
validada mediante tests
8. Modelar TABLE como PayrollObject

Se mantiene la decisión:

TABLE es un PayrollObject
BK canónica:
ruleSystemCode + objectTypeCode + objectCode
9. Introducir estructura común de filas de tabla

Se crea una estructura física común:

payroll_table_row

Campos
ruleSystemCode
tableCode
searchCode
startDate
endDate
annualValue
monthlyValue
dailyValue
hourlyValue
active
Identidad funcional
(ruleSystemCode, tableCode, searchCode, startDate)
Propósito
Lookup eficiente por clave + fecha
Soporte para tablas típicas de nómina:
salario base
plus convenio
antigüedad
10. Estrategia de valores

Cada tabla define un valueBasis:

Ejemplo:

MONTHLY_MASTER
ANNUAL_MASTER

Se establece:

un valor rector
valores derivados persistidos
11. Relación convenio → tabla

Se define mediante binding, no por estructura interna de la tabla.

Ejemplo:

AGREEMENT → BASE_SALARY_TABLE → TABLE SB_RETAIL

La tabla:

no necesita incluir agreementCode en su clave
puede reutilizarse o especializarse libremente
12. Estrategia de evolución

Se permite que en el futuro existan nuevos tipos de objeto payroll:

Ejemplo:

COMPLEX_TABLE

Motivación:

no forzar todos los casos en un único modelo de tabla
permitir crecimiento sin romper diseño base
Consecuencias
Positivas
Enriquecimiento del convenio sin romper catálogo existente
Eliminación de constantes duras (ej. 2000 horas)
Integración natural convenio ↔ payroll
Modelo escalable basado en contextos
Evita proliferación de tablas específicas por tipo de entidad
Separación clara entre:
activación (qué se calcula)
binding (de dónde salen los datos)
Lookup de tablas eficiente y uniforme
Negativas / Riesgos
Introducción de dos nuevas tablas genéricas (activation y binding)
Necesidad de disciplina en bindingRoleCode
Riesgo de sobreuso de TABLE para casos complejos
Precedencia no configurable en V1 (requiere cambios de código)
No objetivos (V1)
Modelado completo de versiones de convenio
Modelado genérico de tablas multiclave complejas
Engine de reglas declarativas completo
Precedencia configurable en base de datos
Activación de objetos distintos de PAYROLL_CONCEPT
UI avanzada de configuración payroll
Estrategia de implementación
Crear agreement_profile
Integrar annualHours en working_time
Crear payroll_object_activation
Crear payroll_object_binding
Crear TABLE + payroll_table_row para salario base
Activar SALARIO_BASE desde convenio
Resolver salario base en payroll usando:
convenio
categoría
tabla vinculada
fecha
Nota operativa

Se recomienda iniciar el uso de:

convenios reales (código oficial)
datos reales de tablas salariales

Manteniendo datos actuales como:

entorno de test
fallback

Esto permitirá validar el modelo con casos reales desde el inicio.

🧠 Cierre

La idea central de este ADR es:

El convenio no calcula nómina, pero sí define el contexto que activa qué se calcula y con qué datos.

Y el sistema se organiza en torno a tres pilares:

Contexto (agreement, company, etc.)
Activación (conceptos)
Binding (fuentes)