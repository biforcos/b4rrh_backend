ADR — COMPANY como catálogo reutilizable enriquecido mediante profile y anclado técnicamente a rule_entity
Estado

Propuesto

Contexto

B4RRHH dispone de un metamodelo funcional en el bounded context rulesystem basado en:

rule_system
rule_entity_type
rule_entity

Este metamodelo ya se utiliza como base de validación y parametrización de múltiples verticales del sistema.

Además, el proyecto ya ha fijado varias decisiones relevantes:

1. Los conceptos reutilizables deben nombrarse por su significado funcional real

Se ha decidido que un rule_entity_type debe nombrar el concepto funcional real y no la primera vertical donde apareció. En ese marco, COMPANY es un ejemplo explícito de catálogo reutilizable de dominio, junto con WORK_CENTER, COST_CENTER o COUNTRY.

2. rule_entity no debe tratarse como un CRUD plano

El mantenimiento de rule_entity ya se ha definido como catálogo con vigencia temporal ligera, con identidad funcional basada en:

ruleSystemCode
ruleEntityTypeCode
code
startDate

y con operaciones canónicas de create, get by business key, correct, close y delete restringido.

3. Las APIs públicas del proyecto deben usar business keys, mientras que los IDs técnicos quedan encapsulados en persistencia

Esta regla ya está consolidada en el proyecto y se aplica de forma clara en el modelo de empleado: la identidad pública es funcional, mientras que la persistencia usa claves técnicas para FKs, joins y wiring interno.

4. En el dominio de empleado, el patrón canónico distingue entre identidad pública y persistencia técnica

Por ejemplo, employee.contact se identifica públicamente por employee + contactTypeCode, mientras que internamente la tabla usa id técnico y FK a employee.employee.id. Ese id técnico no define la identidad funcional del recurso, pero sí su anclaje persistente.

Problema

COMPANY nace correctamente como un catálogo reutilizable del metamodelo. Sin embargo, al evolucionar el producto aparece una necesidad real: una empresa no solo necesita:

código
literal visible
vigencia

sino también una ficha ampliada con datos ricos, por ejemplo:

nombre legal
identificador fiscal
dirección

Esto genera una tensión de diseño.

Si COMPANY se mantiene exclusivamente como rule_entity

El modelo queda demasiado pobre para soportar información empresarial básica.

Si COMPANY se promociona inmediatamente a una nueva vertical/autonomía completa

Se corre el riesgo de introducir complejidad prematura y de abrir una familia entera de subdominios (organization.company, organization.work_center, organization.cost_center, etc.) antes de que exista una necesidad operativa clara.

Si se modela la ampliación rica solo con business keys y sin anclaje técnico interno

Se introduciría una excepción innecesaria respecto a la filosofía ya consolidada en el proyecto, que separa:

identidad pública funcional
identidad interna/persistente técnica

El sistema necesita una solución intermedia, evolutiva y coherente con las decisiones ya tomadas.

Decisión

Se adopta para COMPANY el siguiente modelo:

1. COMPANY seguirá siendo un catálogo reutilizable del metamodelo

COMPANY se mantiene como rule_entity_type reutilizable y sus ocurrencias continúan viviendo en rulesystem.rule_entity.

Su responsabilidad sigue siendo:

identidad catalogal funcional
código reutilizable
label visible
vigencia
activación

Esto preserva el papel de COMPANY como concepto reusable en múltiples verticales y workflows.

2. La ficha ampliada de empresa no se modelará dentro de rule_entity

Los datos ricos de empresa no se introducirán como extensión ad hoc de rule_entity.

Se crea un recurso complementario específico para la ampliación rica de la empresa.

Nombre conceptual adoptado:

company_profile

Su responsabilidad es representar la ficha ampliada de una empresa sin alterar la naturaleza catalogal base de rule_entity.

3. company_profile se anclará técnicamente a rule_entity.id

La relación interna se resuelve mediante FK técnica a la ocurrencia base de rule_entity de tipo COMPANY.

Es decir:

la identidad pública seguirá usando business keys
la persistencia interna usará un anclaje técnico estable
Regla adoptada

company_profile referencia internamente a la empresa base mediante:

company_rule_entity_id → FK a rulesystem.rule_entity.id

Esto sigue la misma filosofía ya utilizada en el modelo de empleado:

business key fuera
FK técnica dentro
4. Alcance funcional V1 de company_profile

Para la primera iteración, company_profile solo cubrirá:

legalName
taxIdentifier
dirección

La dirección podrá modelarse inicialmente como campos simples embebidos en el profile.

No se incluyen todavía en V1:

numeración de empleados
teléfonos
emails
contactos por tipo
políticas avanzadas
subverticales de company
5. Teléfono y email quedan explícitamente fuera de V1

Aunque podrían modelarse como columnas simples, se decide no hacerlo en esta fase.

Justificación:

el proyecto ya ha consolidado en employee.contact un patrón semántico claro para canales de contacto: slot por tipo, validación por catálogo y separación respecto a la ficha base del sujeto.
introducir phone y email como dos campos planos en company_profile sería una simplificación aceptable a muy corto plazo, pero introduciría una asimetría conceptual innecesaria.
se prefiere aplazar esta decisión hasta que exista necesidad real de contacto empresarial, momento en el cual podrá evaluarse si procede una solución equivalente a contactos por tipo.
6. No se crea todavía una nueva vertical/autonomía completa de organización

Esta decisión no introduce todavía:

bounded context organization
vertical completa organization.company
subverticales como organization.company.contact, organization.company.address, organization.company.numbering_policy

La decisión actual se limita a:

mantener COMPANY como catálogo reutilizable
permitir una ampliación rica controlada mediante company_profile
Diseño funcional adoptado
Naturaleza de COMPANY

COMPANY pasa a entenderse como un concepto de dos capas:

A. Capa catalogal canónica

Representada por rulesystem.rule_entity

Responsabilidad:

identidad reusable
code
name
vigencia
activación
B. Capa de profile enriquecido

Representada por company_profile

Responsabilidad:

ficha ampliada
datos operativos básicos
evolución gradual sin contaminar el metamodelo
Identidad
Identidad pública de la empresa

La identidad pública funcional sigue siendo:

ruleSystemCode
companyCode

donde:

companyCode es el rule_entity.code
ruleEntityTypeCode = COMPANY
Identidad interna de persistencia

La persistencia interna se apoya en:

rulesystem.rule_entity.id como root técnico base del concepto catalogal
company_profile.id como PK técnica propia del profile
company_profile.company_rule_entity_id como FK técnica única hacia rule_entity.id
Persistencia recomendada
Tabla base existente

rulesystem.rule_entity

Con ocurrencias de:

ruleEntityTypeCode = COMPANY
Nueva tabla propuesta

company_profile

Campos iniciales recomendados:

id
company_rule_entity_id
legal_name
tax_identifier
street
city
postal_code
region_code
country_code
created_at
updated_at
Restricciones recomendadas
PK técnica en company_profile.id
FK obligatoria:
company_rule_entity_id -> rulesystem.rule_entity.id
unique:
company_rule_entity_id
validación de que la rule_entity referenciada sea de tipo COMPANY
API pública
Principio general

Las APIs públicas siguen usando business keys, nunca IDs técnicos. Esto mantiene coherencia con la convención general del proyecto.

API de catálogo base

Se mantiene el mantenimiento canónico de rule_entity ya definido:

POST /rule-entities
GET /rule-entities
GET /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}
PUT /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}
POST /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}/close
DELETE /rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}
API de profile enriquecido

Se introduce una API específica orientada a la ficha ampliada de empresa.

Endpoints recomendados:

GET /companies/{ruleSystemCode}/{companyCode}/profile
PUT /companies/{ruleSystemCode}/{companyCode}/profile

Opcionalmente, si compensa por ergonomía:

GET /companies/{ruleSystemCode}/{companyCode}

como endpoint agregado de lectura enriquecida.

Reglas de dominio
1. Separación de responsabilidades
rule_entity define la identidad catalogal canónica
company_profile define la ampliación rica
2. No duplicar semántica de identidad

company_profile no define una nueva identidad pública de empresa.

3. No mezclar identidad pública y wiring interno
el exterior usa ruleSystemCode + companyCode
el interior usa FK técnica a rule_entity.id
4. company_profile no reemplaza a rule_entity

No puede existir empresa operativamente válida sin su base catalogal correspondiente.

5. La vigencia canónica sigue residiendo en rule_entity

No se traslada a company_profile una lógica temporal propia en esta fase.

6. taxIdentifier podrá evolucionar

En V1 se modela como dato simple, pero el diseño permite introducir más adelante validaciones específicas por país o regla sin romper la arquitectura.

Relación con el crecimiento de rule_system

Esta decisión se considera importante porque establece una vía general para la evolución del metamodelo.

Patrón emergente

Un concepto del metamodelo puede recorrer estas fases:

Fase 1 — Catálogo puro

Solo requiere:

code
name
vigencia
Fase 2 — Catálogo + profile enriquecido

El concepto sigue siendo reusable, pero necesita ficha ampliada.

Fase 3 — Vertical/autonomía plena

Solo cuando además aparecen:

operaciones canónicas propias
invariantes fuertes propias
UX específica de mantenimiento
procesos donde el concepto es sujeto funcional

Esta progresión evita dos errores:

dejar conceptos ricos empobrecidos en el catálogo
convertir demasiado pronto cualquier catálogo importante en un subdominio grande
Consecuencias positivas
mantiene a COMPANY como concepto reusable y estable del metamodelo
evita sobrecargar rule_entity con atributos ricos no propios de un catálogo
mantiene coherencia con la filosofía general del proyecto: business key fuera, surrogate key dentro
abre una senda de crecimiento sana para otros conceptos del rulesystem
permite enriquecer “datos de empresa” sin crear todavía una arquitectura organizativa prematura
se alinea con patrones ya conocidos en sistemas como HRAccess, donde el identificador técnico del catálogo funciona como anclaje FK en estructuras derivadas
Costes / riesgos
introduce una nueva tabla y lógica de resolución adicional
obliga a mantener clara la frontera entre catálogo y profile
deja abierta una futura decisión sobre contactos empresariales
puede requerir refactor si en el futuro COMPANY adquiere procesos y operaciones suficientes para convertirse en vertical plena
Alternativas consideradas
1. Mantener todo en rule_entity

Descartado.

Se queda corto para modelar datos empresariales básicos y empuja a usar el catálogo como contenedor genérico.

2. Crear ya organization.company como vertical plena

Descartado por prematuro.

No hay todavía suficientes operaciones, invariantes ni semántica propia para justificar esa promoción.

3. Modelar company_profile solo con business keys y sin FK técnica

Descartado.

Rompe innecesariamente la filosofía ya consolidada en el proyecto respecto a la separación entre identidad pública y persistencia interna.

4. Meter phone/email como campos planos en V1

Aplazado.

Posible, pero no deseable mientras no se aclare la estrategia de contactos empresariales.

No objetivos

Este ADR no introduce todavía:

modelo de numeración de empleados
vertical de contactos de empresa
vertical de direcciones de empresa con historización
bounded context organization
jerarquía organizativa
promoción de COMPANY a aggregate root autónomo
sincronización automática compleja entre catálogo y profile más allá de su relación estructural
Estrategia recomendada de implementación
Fase 1
mantener COMPANY en rule_entity
crear tabla company_profile
FK única a rule_entity.id
exponer lectura y actualización del profile
Fase 2
enriquecer frontend de “datos de empresa”
mostrar lectura agregada catálogo + profile
introducir validaciones ligeras de taxIdentifier
Fase 3
evaluar contactos empresariales
evaluar si algunos conceptos de company merecen profile adicional o vertical propia
Fase 4
revisar si COMPANY sigue siendo “catálogo + profile” o si ya ha madurado hasta necesitar vertical/autonomía plena
Resumen ejecutivo

COMPANY seguirá siendo en B4RRHH un catálogo reutilizable del metamodelo (rule_entity), porque su identidad canónica y su reutilización transversal así lo justifican.

Sin embargo, cuando la empresa necesite una ficha ampliada, esta no se modelará dentro de rule_entity, sino mediante un recurso complementario company_profile.

company_profile se anclará internamente mediante FK técnica a rulesystem.rule_entity.id, preservando la misma filosofía que ya se usa en employee: business keys en la API pública, IDs técnicos solo en persistencia.

La primera versión del profile se limitará a:

nombre legal
identificador fiscal
dirección

y dejará fuera, de momento, numeración y contactos empresariales.

Esta decisión no crea todavía un nuevo universo organization.*, pero sí fija una vía muy importante para el crecimiento del rulesystem: catálogo reusable → profile enriquecido → posible vertical plena solo si el dominio lo exige.