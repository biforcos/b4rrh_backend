ADR — Mantenimiento de rule_entity en B4RRHH
Estado

Propuesto

Contexto

B4RRHH ya dispone de un metamodelo funcional basado en:

rule_system
rule_entity_type
rule_entity

Actualmente el contrato expone para rule_entity:

POST /rule-entities
GET /rule-entities con filtros por business keys

y el modelo público incluye:

ruleSystemCode
ruleEntityTypeCode
code
name
description
active
startDate
endDate

La pantalla de catálogos ya permite:

seleccionar rule_system
seleccionar rule_entity_type
listar rule_entity
crear nuevos valores de catálogo

Sin embargo, todavía no existe una estrategia explícita de mantenimiento para:

corregir una ocurrencia existente
cerrar su vigencia
eliminar ocurrencias erróneas sin uso

Además, B4RRHH ya distingue en frontend entre:

edición tipo SLOT
mantenimiento temporal
corrección administrativa frente a cambio funcional real

También existe ya una decisión previa de naming/semántica: rule_entity_type debe nombrar el concepto funcional real y rule_entity.code debe ser estable, mientras que name actúa como label funcional visible

Problema

Si rule_entity se trata como un CRUD plano, aparecen varios riesgos:

confusión entre identidad y datos corregibles
pérdida de histórico semántico
borrados peligrosos de valores de catálogo ya usados por empleados u otros recursos
un frontend que muestra verbos genéricos sin reflejar la semántica real del dominio

Por el contrario, si se prohíbe todo mantenimiento salvo el alta, el catálogo queda operativamente incompleto.

Es necesario definir:

qué constituye la identidad funcional de una ocurrencia de rule_entity
qué operaciones canónicas existen
qué se puede corregir
cuándo procede cerrar
si existe DELETE, en qué condiciones
Decisión

Se adopta para rule_entity un modelo de mantenimiento de catálogo con vigencia y borrado excepcional restringido.

1. Naturaleza funcional

rule_entity se modela como un catálogo parametrizable con vigencia temporal ligera:

puede tener histórico por código
no exige cobertura continua
no debe tratarse como CRUD plano
no debe confundirse corrección administrativa con cambio funcional
2. Identidad funcional

La identidad funcional de una ocurrencia de rule_entity será:

ruleSystemCode
ruleEntityTypeCode
code
startDate

Esta combinación identifica una ocurrencia concreta del valor de catálogo.

3. Campos inmutables

Una vez creada la ocurrencia, no podrán modificarse:

ruleSystemCode
ruleEntityTypeCode
code
startDate
4. Campos corregibles

Podrán corregirse:

name
description
endDate
5. Tratamiento de active

active se considera preferentemente un dato derivado/read-model a partir de la vigencia real.

Mientras el contrato público lo mantenga, backend podrá seguir retornándolo, pero el mantenimiento canónico no debe apoyarse en editar active de forma arbitraria si eso duplica la semántica de endDate.

6. Operaciones canónicas
6.1 Crear

Se mantiene:

POST /rule-entities
6.2 Consultar una ocurrencia concreta

Se añade una lectura canónica por business key completa:

GET /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}
6.3 Corregir una ocurrencia existente

Se añade una operación de corrección administrativa:

PUT /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}

Esta operación corrige la misma ocurrencia y no crea una nueva.

Campos permitidos en request:

name
description
endDate
6.4 Cerrar vigencia

Se añade una operación explícita de cierre:

POST /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}/close

Request:

endDate

Su semántica es cerrar la vigencia de la ocurrencia existente.

6.5 Eliminar

Se admite DELETE, pero como operación excepcional y restringida, no como verbo principal de mantenimiento:

DELETE /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}

Su semántica es borrado físico de una ocurrencia de catálogo solo si backend demuestra que no está usada.

Reglas de borrado

DELETE solo estará permitido si se cumplen todas las condiciones siguientes:

La ocurrencia existe.
La comprobación se realiza dentro del rule_system de la ocurrencia.
La rule_entity no está referenciada por ningún recurso de negocio existente que dependa de ella.
La comprobación de referencias debe hacerse en backend, nunca en frontend.
Si existen referencias, la operación falla con conflicto de negocio y no degrada a soft delete implícito.
Política de referencias

Se considera “referenciada” una rule_entity cuando su código está siendo usado por cualquier recurso real que la valide o consuma en ese rule_system.

Ejemplos típicos:

companyCode en presence
contactTypeCode en contacts
identifierTypeCode en identifiers
addressTypeCode en addresses
workCenterCode en work centers
costCenterCode en cost centers
contractCode o contractSubtypeCode en contracts
agreementCode o agreementCategoryCode en labor classifications

La comprobación exacta dependerá del ruleEntityTypeCode y de los verticales que consuman ese catálogo.

Reglas de dominio adicionales
no puede haber solape de vigencia para la misma combinación ruleSystemCode + ruleEntityTypeCode + code
endDate no puede ser menor que startDate
una corrección administrativa no debe alterar la identidad funcional
un cierre expresa fin de vigencia, no borrado
un cambio funcional normal puede resolverse como cierre de la ocurrencia vigente y alta de una nueva ocurrencia
Semántica de frontend

El frontend de catálogos debe exponer acciones honestas y alineadas con backend:

Crear
Editar
entendido como corrección administrativa de la misma ocurrencia
Cerrar
Eliminar
solo cuando backend lo soporte y sujeto a error si existen referencias

El frontend no debe:

editar rule_entity_type
cambiar la identidad funcional de una ocurrencia
simular borrados si backend no los confirma
ocultar el motivo de rechazo cuando una entity no puede borrarse por estar en uso
Errores esperados
404 Not Found

Cuando la ocurrencia concreta no exista.

409 Conflict

Cuando:

haya referencias activas o históricas que impidan el borrado
el cierre o corrección rompa reglas temporales
se intente dejar la ocurrencia en un estado inconsistente
No objetivos

Este ADR no introduce todavía:

mantenimiento frontend de rule_entity_type
renombrado masivo de tipos o seeds
versionado complejo de catálogos
soft delete genérico
cascadas automáticas de cleanup
Consecuencias positivas
mantenimiento realista de rule_entity
histórico preservado cuando corresponde
borrado físico posible para errores sin uso
menor riesgo de destruir datos referenciados
frontend con verbos honestos y semánticos
Consecuencias negativas
backend necesita lógica de comprobación de referencias
DELETE deja de ser trivial
algunos casos requerirán decidir si aplicar correct, close o delete
aparece coste de diseño por tipo de catálogo consumidor
Estrategia de implementación
Fase 1

Backend:

GET by business key
PUT correct
POST close
DELETE con comprobación de referencias
Fase 2

Frontend:

abrir detalle/edición de ocurrencia concreta
soportar editar
soportar cerrar
soportar eliminar con confirmación ligera
Fase 3

Refinamiento:

mensajes de conflicto por entidad en uso
posible visibilidad de motivo de bloqueo
tests por tipo consumidor
Resumen

rule_entity no se gestionará como CRUD plano.

Su mantenimiento canónico en B4RRHH será:

create
get by business key
correct
close
delete restringido

DELETE existirá, pero únicamente como operación excepcional y segura, protegida por validación backend de ausencia total de referencias dentro del rule_system.