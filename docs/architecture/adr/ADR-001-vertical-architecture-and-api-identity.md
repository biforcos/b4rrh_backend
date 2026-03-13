# ADR — Arquitectura por verticales y reglas de identidad API en B4RRHH

## Estado
Propuesta adoptada como guía de refactor y convención base del proyecto.

## Objetivo
Definir de forma inequívoca cómo debe organizarse el código en B4RRHH, cómo deben diseñarse las APIs y qué decisiones deben seguirse al crear o refactorizar verticales funcionales, para evitar desviaciones de implementación al trabajar con Copilot o al crecer el proyecto.

---

# 1. Contexto

B4RRHH está evolucionando desde una estructura inicialmente más centrada en capas globales (`application`, `domain`, `infrastructure`) hacia un modelo donde el negocio ya no es un único bloque homogéneo, sino un conjunto de verticales funcionales dentro de bounded contexts claros.

En la práctica, ya existen varios subdominios o verticales relevantes:

- `employee.employee`
- `employee.presence`
- `employee.contact`
- `rulesystem.rule_system`
- `rulesystem.rule_entity_type`
- `rulesystem.rule_entity`

A medida que el proyecto crezca, aparecerán más verticales y recursos relacionados con el empleado, por ejemplo:

- `employee.address`
- `employee.document`
- `employee.assignment`
- `employee.bank_account`
- `employee.compensation`
- etc.

La estructura actual mezcla dos criterios de organización:

1. organización por capas globales
2. organización por verticales con capas internas

Esa mezcla genera asimetrías, dificulta la navegación, favorece decisiones inconsistentes en API y aumenta la probabilidad de que Copilot implemente nuevos verticales siguiendo patrones incorrectos.

Este ADR fija el modelo objetivo.

---

# 2. Decisión arquitectónica principal

## 2.1. Regla principal

**En B4RRHH, el código se organiza primero por vertical/subdominio, y dentro de cada vertical se aplica arquitectura hexagonal.**

Eso significa que el eje principal del scaffolding es el negocio, no las capas globales.

## 2.2. Consecuencia práctica

No se debe seguir creciendo con una estructura donde, dentro de un mismo bounded context, convivan simultáneamente:

- paquetes raíz por capa (`application`, `domain`, `infrastructure`)
- y paquetes raíz por vertical (`presence`, `contact`, etc.)

Ese híbrido sólo se tolera como estado transitorio durante la migración.

## 2.3. Modelo objetivo

Cada bounded context se organiza en verticales. Cada vertical contiene sus propias capas hexagonales:

- `application`
- `domain`
- `infrastructure`

Opcionalmente puede tener subpaquetes como:

- `application.usecase`
- `application.port`
- `application.service`
- `domain.model`
- `domain.port`
- `domain.exception`
- `infrastructure.persistence`
- `infrastructure.web`
- `infrastructure.web.dto`

---

# 3. Estructura objetivo del proyecto

## 3.1. Estructura conceptual de alto nivel

```text
com.b4rrhh
  employee
    employee
    presence
    contact
    shared
  rulesystem
  shared
```

## 3.2. Estructura objetivo detallada para `employee`

```text
com.b4rrhh.employee
  employee
    application
      port
      service
      usecase
    domain
      model
      port
      exception
    infrastructure
      persistence
      web
        dto

  presence
    application
      port
      service
      usecase
    domain
      model
      port
      exception
    infrastructure
      persistence
      web
        dto

  contact
    application
      port
      service
      usecase
    domain
      model
      port
      exception
    infrastructure
      persistence
      web
        dto

  shared
    application
    domain
    infrastructure
```

## 3.3. Estado de `rulesystem`

`rulesystem` puede mantenerse temporalmente con su estructura actual si no compensa refactorizarlo ahora mismo.

No obstante, **todo vertical nuevo dentro de `employee` debe seguir ya el modelo vertical-first**, y los verticales existentes deben migrarse de forma incremental.

---

# 4. Regla de identidad en APIs

## 4.1. Regla obligatoria del proyecto

**Todas las APIs de B4RRHH deben trabajar con códigos funcionales de dominio. Nunca con IDs técnicos como identidad pública del recurso.**

Esta es una convención global del proyecto y aplica a todos los bounded contexts y verticales.

## 4.2. Qué significa “código funcional”

Son identificadores de negocio estables y significativos, por ejemplo:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber`
- `contactTypeCode`
- `ruleEntityTypeCode`
- `ruleEntityCode`

## 4.3. Qué no debe exponerse en la API

No deben utilizarse como identidad pública en paths ni en la semántica de la API:

- `id`
- `employeeId`
- `contactId`
- `presenceId`
- claves surrogate de base de datos
- UUIDs técnicos sin valor de negocio

Los IDs técnicos pueden existir y seguir existiendo para:

- persistencia
- joins
- rendimiento
- claves primarias internas
- simplificación de adapters y repositorios

Pero no deben dirigir la forma de la API pública.

## 4.4. Regla de consistencia

No se permite mezclar en una misma API:

- recurso padre identificado por business key
- recurso hijo identificado por id técnico

Tampoco al revés.

Si un recurso tiene identidad funcional clara, la API debe expresarla.

---

# 5. Regla de modelado de recursos

## 5.1. Los recursos se modelan por su identidad funcional real

Al diseñar un vertical, primero debe responderse a estas preguntas:

1. ¿cuál es la identidad funcional del recurso?
2. ¿qué campos forman parte de esa identidad?
3. ¿qué campos son mutables?
4. ¿qué campos son meramente persistentes o técnicos?
5. ¿el recurso es historizado o no?
6. ¿hay unicidad por tipo, período o combinación de códigos?

## 5.2. No confundir identidad con persistencia

Si un recurso tiene un `id` técnico en base de datos, eso no implica que su identidad de negocio sea ese `id`.

Ejemplo:

- `employee.contact` puede tener columna `id`
- pero su identidad funcional puede ser `employee + contactTypeCode`

## 5.3. Los endpoints deben expresar el dominio

Cuando una regla de negocio diga “sólo puede existir uno por tipo”, la API debe tender a expresarlo como tal, en lugar de simular una colección anónima de filas con `id`.

---

# 6. Convenciones específicas para el bounded context `employee`

## 6.1. Verticales actuales

Dentro de `employee`, por ahora se consideran verticales explícitos:

- `employee`
- `presence`
- `contact`

A futuro podrán añadirse otros verticales del mismo nivel.

## 6.2. Regla de naming

Se prioriza naming orientado a negocio y no a artefacto técnico.

Buenos ejemplos:

- `CreateContactUseCase`
- `UpdateContactService`
- `ContactRepository`
- `ContactBusinessKeyController`
- `PresenceCatalogValidator`

Evitar nombres que consoliden decisiones incorrectas de identidad, por ejemplo:

- `GetContactByIdUseCase`
- `DeletePresenceByIdUseCase`
- `EmployeeIdController`

salvo que el caso sea estrictamente interno y no forme parte de la API pública.

## 6.3. `shared` dentro de `employee`

El paquete `employee.shared` sólo debe contener elementos verdaderamente transversales al bounded context y sin pertenencia clara a un vertical concreto.

No debe convertirse en un cajón desastre.

Se debe evitar mover a `shared`:

- lógica de dominio específica de un vertical
- validaciones concretas de un recurso
- DTOs
- queries o repositorios de un subdominio concreto

---

# 7. Caso de referencia: `employee.contact`

Este vertical se usará como patrón canónico del refactor.

## 7.1. Naturaleza del recurso

`employee.contact` representa medios de contacto actuales de un empleado.

## 7.2. Reglas funcionales acordadas

- no historizado
- un solo contacto por tipo por empleado
- `contact_type_code` validado contra `rulesystem.rule_entity`
- `rule_entity_type_code = EMPLOYEE_CONTACT_TYPE`
- tipos de contacto definidos por `rule_system`
- `contact_value` obligatorio
- validación ligera del valor según tipo
- borrado físico

## 7.3. Identidad funcional del contacto

La identidad funcional del recurso es:

- empleado
- `contactTypeCode`

El contacto no se identifica funcionalmente por un `contactId` técnico.

## 7.4. Mutabilidad

- `contactTypeCode`: **inmutable** tras creación
- `contactValue`: **mutable**

## 7.5. Persistencia

Puede existir una tabla como:

- `employee.contact(id, employee_id, contact_type_code, contact_value, created_at, updated_at)`

con restricción:

- `unique(employee_id, contact_type_code)`

Eso es correcto siempre que se entienda que:

- `id` es técnico
- la identidad funcional del recurso no es ese `id`

## 7.6. API objetivo para `employee.contact`

La API debe trabajar exclusivamente con business keys del empleado y del tipo de contacto.

### Endpoints objetivo

```text
POST   /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts
GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts
GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}
PUT    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}
DELETE /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}
```

## 7.7. DTOs recomendados

### CreateContactRequest
Debe contener:

- `contactTypeCode`
- `contactValue`

### UpdateContactRequest
Debe contener sólo:

- `contactValue`

No debe permitir cambiar `contactTypeCode`.

### ContactResponse
Debe evitar exponer IDs técnicos como identidad principal del recurso. Si un campo técnico se mantiene temporalmente por motivos internos, debe tratarse como excepción transitoria, no como convención.

## 7.8. Estructura objetivo del paquete `contact`

```text
com.b4rrhh.employee.contact
  application
    port
      EmployeeContactLookupPort.java
      EmployeeContactContext.java
    service
      ContactCatalogValidator.java
    usecase
      ContactRuleEntityTypeCodes.java
      CreateContactCommand.java
      CreateContactService.java
      CreateContactUseCase.java
      DeleteContactCommand.java
      DeleteContactService.java
      DeleteContactUseCase.java
      GetContactByBusinessKeyService.java
      GetContactByBusinessKeyUseCase.java
      ListEmployeeContactsService.java
      ListEmployeeContactsUseCase.java
      UpdateContactCommand.java
      UpdateContactService.java
      UpdateContactUseCase.java

  domain
    model
      Contact.java
    port
      ContactRepository.java
    exception
      ContactAlreadyExistsException.java
      ContactCatalogValueInvalidException.java
      ContactEmployeeNotFoundException.java
      ContactNotFoundException.java
      ContactRuleSystemNotFoundException.java
      ContactValueInvalidException.java

  infrastructure
    persistence
      ContactEntity.java
      ContactPersistenceAdapter.java
      EmployeeContactLookupAdapter.java
      SpringDataContactRepository.java
    web
      ContactBusinessKeyController.java
      ContactExceptionHandler.java
      dto
        ContactErrorResponse.java
        ContactResponse.java
        CreateContactRequest.java
        UpdateContactRequest.java
```

---

# 8. Caso de referencia: `employee.presence`

`employee.presence` debe tender al mismo modelo arquitectónico que `employee.contact`, aunque sus reglas funcionales sean distintas.

## 8.1. Naturaleza

- vertical hermano de `employee.contact`
- no un subpaquete accidental dentro de una arquitectura por capas globales

## 8.2. Acción recomendada

Una vez estabilizado `contact` como patrón, `presence` debe revisarse para alinearse con la misma convención:

- vertical en primer nivel del bounded context `employee`
- hexagonal interna
- endpoints públicos basados en business keys

---

# 9. Validación contra catálogos (`rule_entity`)

## 9.1. Principio

Las validaciones de catálogo deben seguir el metamodelo existente del proyecto.

## 9.2. Regla

Cuando un campo de un vertical representa un código parametrizable, debe validarse contra `rulesystem.rule_entity` usando:

- `ruleSystemCode` correcto
- `ruleEntityTypeCode` correcto
- `code` correcto

## 9.3. Sobre activo y vigencia

Es aceptable reutilizar una validación genérica común que además compruebe:

- activo
- vigencia temporal

si eso forma parte de la infraestructura compartida del metamodelo.

Pero debe entenderse como:

- una política de validación técnica compartida
- no necesariamente como una característica específica del vertical en cuestión

No debe complicarse el modelo funcional del recurso sólo por heredar esa validación compartida.

---

# 10. Regla sobre seeds por `rule_system`

## 10.1. Decisión actual

Los catálogos como `EMPLOYEE_CONTACT_TYPE` pueden repetirse por `rule_system`, aunque hoy los valores coincidan entre sistemas.

## 10.2. Justificación

Se evita introducir por ahora una jerarquía más compleja de catálogos globales / por país / por familia.

## 10.3. Consecuencia

Es válido sembrar valores por cada `rule_system` existente en una migración inicial.

## 10.4. Deuda conocida

Debe definirse en el futuro cómo escalar esto cuando se creen nuevos `rule_system`:

- seed automático al alta
- proceso operativo
- estrategia de bootstrap
- otro mecanismo

Esta deuda no invalida el diseño actual, pero debe permanecer visible.

---

# 11. Reglas de diseño para Copilot

Estas reglas deben incluirse en prompts de implementación o refactor.

## 11.1. Reglas obligatorias

1. Organiza el código primero por vertical/subdominio.
2. Dentro de cada vertical, aplica arquitectura hexagonal.
3. No mezcles paquetes raíz por capa y por vertical dentro de un mismo bounded context.
4. Nunca expongas IDs técnicos en APIs públicas si existe una identidad funcional clara.
5. Usa siempre códigos funcionales en paths y contratos OpenAPI.
6. Mantén los IDs técnicos sólo en persistencia y wiring interno.
7. Cuando un recurso tenga unicidad funcional por combinación de códigos, exprésala en el diseño del endpoint.
8. No permitas mutar campos que formen parte de la identidad funcional.
9. Actualiza OpenAPI, casos de uso, adapters, tests y documentación de recurso en cada refactor.
10. No introduzcas historización si el recurso no la requiere.

## 11.2. Antipatrones a evitar

Copilot no debe:

- crear un nuevo paquete raíz suelto al lado de `application`, `domain`, `infrastructure` cuando el bounded context ya tiene verticales
- exponer endpoints por `{id}` cuando el dominio ya tiene business keys claras
- usar DTOs de update que permitan modificar campos identificativos
- tratar una tabla con surrogate key como si esa surrogate key fuera automáticamente la identidad del recurso
- diseñar recursos como listas de filas genéricas cuando el negocio habla de “uno por tipo” o “uno por combinación de códigos”

---

# 12. Estrategia de migración recomendada

## 12.1. No hacer big bang global

No se recomienda un megarrefactor de todo el proyecto en una sola iteración.

## 12.2. Orden recomendado

1. fijar este ADR como convención
2. refactorizar `employee.contact`
3. usar `employee.contact` como patrón canónico
4. alinear `employee.presence`
5. consolidar `employee.employee` si procede
6. aplicar la convención a nuevos verticales

## 12.3. Regla para nuevos desarrollos

Mientras existan áreas aún no migradas, cualquier vertical nuevo debe ya nacer con la estructura objetivo.

---

# 13. Checklist de revisión para cualquier vertical nuevo

Antes de aceptar una implementación, revisar:

## 13.1. Arquitectura

- ¿el vertical está organizado como vertical autónomo con capas internas?
- ¿se ha evitado mezclar vertical raíz con capas raíz del mismo bounded context?

## 13.2. API

- ¿los endpoints usan business keys?
- ¿hay algún `{id}` técnico expuesto sin necesidad?
- ¿la identidad del path expresa el dominio real?

## 13.3. Dominio

- ¿la identidad funcional está clara?
- ¿qué campos son inmutables?
- ¿qué campos son mutables?
- ¿la unicidad real del negocio está modelada?

## 13.4. Persistencia

- ¿el id técnico queda encapsulado?
- ¿hay unique constraints alineadas con la identidad funcional?

## 13.5. OpenAPI

- ¿los schemas reflejan las reglas de mutabilidad?
- ¿los DTOs de update evitan modificar campos identitarios?

## 13.6. Tests

- ¿hay tests de caso feliz?
- ¿hay tests de duplicado/unicidad?
- ¿hay tests de ownership o pertenencia al recurso padre?
- ¿hay tests de validación de catálogo?
- ¿hay tests de integración con constraints reales de BD?

---

# 14. Prompt base para Copilot — creación/refactor de verticales en B4RRHH

```text
You are working in the B4RRHH project.

Mandatory project conventions:
- Organize code first by business vertical/subdomain, not by global architectural layer.
- Inside each vertical, use hexagonal architecture.
- Public APIs must always use functional business codes, never technical database IDs.
- Technical IDs may exist only for persistence and internal wiring.
- If a resource has a clear functional identity, the REST API must express it explicitly.
- Fields that are part of the functional identity are immutable after creation unless explicitly stated otherwise.
- Always keep OpenAPI, use cases, adapters, tests and resource documentation aligned.

Target package organization pattern:
- com.b4rrhh.<bounded-context>.<vertical>.application...
- com.b4rrhh.<bounded-context>.<vertical>.domain...
- com.b4rrhh.<bounded-context>.<vertical>.infrastructure...

Avoid these mistakes:
- Do not create a new root package for a vertical alongside application/domain/infrastructure inside the same bounded context.
- Do not expose endpoints by technical id when business keys exist.
- Do not allow update DTOs to modify identity fields.
- Do not confuse surrogate database keys with domain identity.

When implementing or refactoring a vertical:
1. Identify the functional business key.
2. Design the REST paths using those business keys.
3. Keep technical ids only in persistence.
4. Define immutable vs mutable fields explicitly.
5. Add database constraints aligned with domain uniqueness.
6. Add tests for duplicates, ownership, catalog validation and persistence constraints.
```

---

# 15. Prompt específico para refactorizar `employee.contact`

```text
Refactor the employee.contact vertical in B4RRHH to comply with the project architecture and API identity rules.

Project rules:
- Code must be organized first by vertical, then by hexagonal layers.
- Public APIs must use functional business codes only, never technical IDs.

Current target architecture:
- com.b4rrhh.employee.contact.application...
- com.b4rrhh.employee.contact.domain...
- com.b4rrhh.employee.contact.infrastructure...

Do not leave contact classes under a mixed structure that combines root layer packages and root vertical packages inside the employee bounded context.

Employee functional identity:
- ruleSystemCode
- employeeTypeCode
- employeeNumber

Contact functional identity within an employee:
- contactTypeCode

Domain rules:
- One contact per contact type per employee.
- contact_type_code is immutable after creation.
- contact_value is mutable.
- Keep technical ids only for persistence.
- Keep unique(employee_id, contact_type_code) in the database.
- Keep contact catalog validation using EMPLOYEE_CONTACT_TYPE.
- Keep the resource non-historized.

Required REST API:
- POST   /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts
- GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts
- GET    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}
- PUT    /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}
- DELETE /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts/{contactTypeCode}

OpenAPI rules:
- CreateContactRequest must contain contactTypeCode and contactValue.
- UpdateContactRequest must contain only contactValue.
- Do not use employeeId or contactId in public API paths.
- Review response DTOs to avoid exposing technical IDs unless strictly internal.

Implementation tasks:
- Move packages to the new structure.
- Rename use cases/services that still express technical-id semantics.
- Replace GetContactById with business-key based retrieval.
- Ensure ownership is enforced through employee business key + contactTypeCode.
- Update controllers, adapters, repository contracts, tests and documentation.
- Keep existing valid persistence model where possible.
```

---

# 16. Consecuencia final

A partir de este ADR:

- el patrón objetivo en `employee` es vertical-first
- las APIs del proyecto se diseñan siempre con business keys
- `employee.contact` se toma como primer vertical a refactorizar con esta convención
- cualquier nuevo vertical debe seguir ya estas reglas desde su nacimiento

Este documento debe usarse como referencia base para diseño humano, revisión técnica y prompts a Copilot.

