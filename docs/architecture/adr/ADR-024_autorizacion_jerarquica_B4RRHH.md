# ADR — Modelo de autorización jerárquica para recursos funcionales en B4RRHH

## Estado
Propuesto

## Decisión principal
La autorización en B4RRHH se modela como un dominio propio basado en:
- roles funcionales
- recursos funcionales jerárquicos
- acciones semánticas
- perfiles de permiso reutilizables
- políticas rol-recurso con herencia y overrides

## Nombre recomendado
`authorization` (preferible a `security` para no mezclar autenticación con autorización)

## Contexto
B4RRHH ya modela el dominio por verticales funcionales, business keys públicas y operaciones honestas. Falta cerrar la autorización con el mismo rigor.

El problema no es solo “añadir roles”, sino resolver:
- lectura global pero mantenimiento parcial por vertical
- workflows permitidos para unos roles y prohibidos para otros
- defaults razonables para recursos nuevos
- extensión a `employee`, `rulesystem` y futuros bounded contexts

## Problema
Un modelo simple de roles por endpoint o por CRUD puro no encaja bien porque:
- la unidad natural del proyecto es el recurso funcional, no el endpoint
- el dominio usa acciones como `CLOSE`, `CORRECT`, `EXECUTE`
- habrá más bounded contexts además de `employee`
- no se quiere reconfigurar cada rol cada vez que nazca una vertical

## Decisión
Se introduce un bounded context técnico-funcional `authorization`.

La cadena lógica del modelo será:

`rol -> política sobre recurso -> perfil de permiso -> acciones permitidas`

El recurso asegurado vive dentro de un árbol jerárquico. Ejemplo:

- `employee`
  - `employee.employee`
  - `employee.contact`
  - `employee.identifier`
  - `employee.address`
  - `employee.presence`
  - `employee.work_center`
  - `employee.working_time`
  - `employee.cost_center`
  - `employee.contract`
  - `employee.labor_classification`
  - `employee.lifecycle`
    - `employee.lifecycle.hire`
    - `employee.lifecycle.terminate`
    - `employee.lifecycle.rehire`
- `rulesystem`
  - `rulesystem.rule_system`
  - `rulesystem.rule_entity_type`
  - `rulesystem.rule_entity`

Los workflows se tratan como recursos de primera clase.

## Principios
1. Separación fuerte entre autenticación y autorización.
2. Recurso funcional como unidad canónica de control.
3. Acciones semánticas honestas.
4. Default + override.
5. Escalabilidad transversal.
6. No modelar autorización por campo como regla general.

## Modelo relacional propuesto

### `authorization.role`
Define roles funcionales.

Campos principales:
- `code`
- `name`
- `description`
- `active`

Ejemplos:
- `ADMIN`
- `HR_MANAGER`
- `HR_OPERATOR`
- `AUDITOR`
- `CATALOG_MANAGER`
- `READONLY`

### `authorization.secured_resource`
Catálogo jerárquico de recursos protegidos.

Campos principales:
- `resource_code`
- `parent_resource_code`
- `bounded_context_code`
- `resource_kind`
- `resource_family_code`
- `name`
- `description`
- `active`

`resource_kind` recomendado:
- `BOUNDED_CONTEXT`
- `VERTICAL`
- `WORKFLOW`
- `GROUP`
- `ADMIN_RESOURCE`

#### Sobre `resource_family_code`

`resource_family_code` es un agrupador funcional de recursos. **No es jerarquía** (eso lo modela `parent_resource_code`) — es agrupación semántica transversal.

**Para qué sirve:**
- Simplificar autorización: en vez de definir 50 reglas por recurso, se definen 5 reglas por familia.
- Evitar explosión de políticas: un rol puede autorizarse sobre una familia entera.
- Permitir reglas transversales: "RRHH ve todo lo de datos de empleado", "Finanzas solo lo económico".

**Familias iniciales recomendadas:**

| `resource_family_code` | Recursos que agrupa |
|------------------------|---------------------|
| `EMPLOYEE_DATA` | employee, contact, identifier, address, contract, labor_classification, presence |
| `ORGANIZATION` | work_center, cost_center |
| `LIFECYCLE` | lifecycle, lifecycle.hire, lifecycle.terminate, lifecycle.rehire |
| `MASTER_DATA` | rulesystem, rule_entity_type, rule_entity |
| `ADMINISTRATION` | authorization y sus sub-recursos |

**Reglas de uso:**
- Todo `secured_resource` debe declarar su `resource_family_code`.
- El catálogo de familias es cerrado y se gobierna mediante ADR o enum en código.
- Las políticas pueden definirse sobre familias en el futuro (extensión de V1, no en V1).
- En V1 `resource_family_code` es campo informativo/filtro de UI; la evaluación jerárquica no lo usa directamente.

### `authorization.action`
Catálogo de acciones.

Acciones iniciales recomendadas:
- `READ`
- `CREATE`
- `UPDATE`
- `DELETE`
- `CLOSE`
- `CORRECT`
- `EXECUTE`
- `ADMIN`

### `authorization.permission_profile`
Perfil reusable de permisos.

Perfiles iniciales recomendados:
- `NONE`
- `READ_ONLY`
- `SLOT_MAINTAINER`
- `TEMPORAL_MAINTAINER`
- `WORKFLOW_EXECUTOR`
- `FULL_CONTROL`

### `authorization.permission_profile_action`
Tabla de composición perfil -> acción.

PK compuesta:
- `permission_profile_code`
- `action_code`

### `authorization.role_resource_policy`
Tabla central del modelo.

Campos principales:
- `role_code`
- `resource_code`
- `permission_profile_code`
- `propagation_mode`
- `active`

PK compuesta:
- `(role_code, resource_code)`

`propagation_mode` recomendado:
- `THIS_RESOURCE_ONLY`
- `THIS_RESOURCE_AND_CHILDREN`

### `authorization.user_role_assignment` (aplazado a V2)
Solo necesaria si B4RRHH persiste roles internos. En V1 los roles del sujeto se extraen del JWT emitido por el IdP externo — B4RRHH no gestiona la asignación de roles, solo la lee del token.

Campos principales (futuros):
- `subject_code`
- `role_code`
- `assignment_origin`
- `active`

## Reglas de modelado
- Todo recurso nuevo que requiera autorización debe registrarse en `authorization.secured_resource`.
- Todo recurso debe declarar, siempre que exista, un `parent_resource_code`.
- Todo recurso debe declarar su `resource_family_code`.
- Los workflows se modelan como recursos de tipo `WORKFLOW`.
- Las políticas se definen sobre recursos, no sobre endpoints.
- La ausencia de permiso implica denegación.
- `NONE` es un perfil que no concede ninguna acción. No es un deny con precedencia sobre otros roles — si otro rol del sujeto concede la acción por otro camino del árbol, la evaluación devuelve ALLOW. `NONE` solo deniega cuando es el único perfil aplicable.
- En V1 no se introducen deny explícitos con precedencia sobre grants de otros roles.
- La autorización por campo queda fuera del modelo base.

## Algoritmo de evaluación
Entrada:
- sujeto autenticado
- roles efectivos
- `resource_code`
- `action_code`

Resolución:
1. Buscar política exacta para `role_code + resource_code`.
2. Si no existe, subir al padre.
3. Repetir hasta la raíz.
4. Cuando se encuentre una política, resolver el perfil.
5. Comprobar si el perfil contiene la acción.
6. Si algún rol concede, permitir.
7. Si ninguno concede, denegar.

Reglas de precedencia:
- el recurso más cercano gana sobre ancestros más lejanos
- la coincidencia exacta gana sobre la heredada
- basta una concesión positiva para permitir
- ausencia de concesión = deny por defecto

## Ejemplos de políticas

### AUDITOR
- `AUDITOR` sobre `employee` -> `READ_ONLY` con propagación a hijos
- `AUDITOR` sobre `rulesystem` -> `READ_ONLY` con propagación a hijos

### HR_OPERATOR
- `HR_OPERATOR` sobre `employee` -> `READ_ONLY` con propagación a hijos
- `HR_OPERATOR` sobre `employee.contact` -> `SLOT_MAINTAINER`
- `HR_OPERATOR` sobre `employee.identifier` -> `SLOT_MAINTAINER`
- `HR_OPERATOR` sobre `employee.address` -> `TEMPORAL_MAINTAINER`
- `HR_OPERATOR` sobre `employee.work_center` -> `TEMPORAL_MAINTAINER`
- `HR_OPERATOR` sobre `employee.working_time` -> `TEMPORAL_MAINTAINER`
- `HR_OPERATOR` sobre `employee.lifecycle.hire` -> `WORKFLOW_EXECUTOR`
- `HR_OPERATOR` sobre `employee.lifecycle.terminate` -> `NONE`
- `HR_OPERATOR` sobre `employee.lifecycle.rehire` -> `NONE`

### HR_MANAGER
- `HR_MANAGER` sobre `employee` -> `READ_ONLY` con propagación a hijos
- `HR_MANAGER` sobre `employee.contact` -> `SLOT_MAINTAINER`
- `HR_MANAGER` sobre `employee.identifier` -> `SLOT_MAINTAINER`
- `HR_MANAGER` sobre `employee.address` -> `TEMPORAL_MAINTAINER`
- `HR_MANAGER` sobre `employee.work_center` -> `TEMPORAL_MAINTAINER`
- `HR_MANAGER` sobre `employee.working_time` -> `TEMPORAL_MAINTAINER`
- `HR_MANAGER` sobre `employee.lifecycle` -> `WORKFLOW_EXECUTOR` con propagación a hijos

### CATALOG_MANAGER
- `CATALOG_MANAGER` sobre `rulesystem.rule_entity` -> `FULL_CONTROL`
- `CATALOG_MANAGER` sobre `rulesystem.rule_entity_type` -> `READ_ONLY`
- `CATALOG_MANAGER` sobre `rulesystem.rule_system` -> `READ_ONLY`

### ADMIN
- `ADMIN` sobre `employee` -> `FULL_CONTROL` con propagación a hijos
- `ADMIN` sobre `rulesystem` -> `FULL_CONTROL` con propagación a hijos
- `ADMIN` sobre `authorization` -> `FULL_CONTROL` con propagación a hijos

## Ejemplo completo de resolución
Caso: `HR_OPERATOR` intenta ejecutar `employee.lifecycle.terminate` con acción `EXECUTE`.

Resolución:
1. Existe política exacta sobre `employee.lifecycle.terminate`.
2. El perfil es `NONE`.
3. `NONE` no contiene `EXECUTE`.
4. Resultado: denegado.

Caso: `HR_MANAGER` intenta ejecutar `employee.lifecycle.terminate` con acción `EXECUTE`.

Resolución:
1. No existe política exacta.
2. Se sube al padre `employee.lifecycle`.
3. Existe perfil `WORKFLOW_EXECUTOR` con propagación a hijos.
4. `WORKFLOW_EXECUTOR` contiene `EXECUTE`.
5. Resultado: permitido.

## Reglas de crecimiento
Cuando nazca una vertical nueva, por ejemplo `employee.bank_account`:
1. se registra en `authorization.secured_resource`
2. se cuelga de `employee`
3. hereda permisos por defecto
4. solo se añade override si el recurso necesita trato especial

## Integración
### Backend
- Spring Security valida el JWT Bearer en cada request (Resource Server con clave simétrica HS256 en V1).
- Los roles del sujeto se extraen del claim `roles` del JWT y se cargan como `GrantedAuthority` en el `SecurityContext`.
- B4RRHH resuelve autorización por `resource_code + action_code` consultando su propio bounded context `authorization`.
- Se expone `POST /authorization/evaluate` que recibe `{ resourceCode, actionCode }` y evalúa con los roles del JWT autenticado.
- La seguridad real vive en backend.

### Frontend
- Puede consultar `POST /authorization/evaluate` para derivar capacidades UI como `canEditContacts` o `canExecuteTerminate`.
- La ocultación de acciones es UX, no seguridad real.

## No objetivos de V1
- CRUD API para gestionar roles, recursos, perfiles y políticas (se gestiona por Flyway).
- Asignación de roles a sujetos desde la API (tabla `user_role_assignment` aplazada).
- Deny explícito con precedencia sobre grants de otros roles.
- Autorización aplicada automáticamente en los endpoints de `employee` (interceptores Spring Security). En V1 solo existe el endpoint de evaluación explícita.

## No objetivos
- autorización contextual por instancia concreta
- seguridad por `ruleSystemCode`, `companyCode` o manager scope en V1
- autorización por campo como modelo base
- detalle del login OIDC y ciclo de vida del token
- deny explícitos con precedencias complejas

## Consecuencias
### Positivas
- alinea autorización con el lenguaje funcional de B4RRHH
- evita acoplar permisos a endpoints
- permite defaults razonables
- admite overrides finos
- trata workflows y verticales bajo un mismo marco
- deja base sólida para auditoría futura

### Costes
- aparece un bounded context adicional
- hay que gobernar el árbol de recursos
- la evaluación jerárquica debe estar muy bien testeada

## Plan de implantación
1. Crear schema `authorization` y semillas base.
2. Implementar evaluación jerárquica en backend.
3. Integrar roles efectivos desde JWT u origen externo.
4. Exponer capacidades derivadas al frontend.
5. Extender a `rulesystem` y futuros bounded contexts.
6. Evaluar futuras extensiones: auditoría, contexto, datos sensibles.

## Resumen ejecutivo
B4RRHH debe modelar la autorización como un dominio propio, separado de la autenticación, apoyado en recursos funcionales jerárquicos. Los roles no conceden permisos sobre endpoints, sino perfiles de permiso sobre recursos del árbol funcional del sistema.

La combinación de recurso jerárquico, perfil reusable y propagación al árbol permite exactamente el equilibrio buscado:
- permisos por defecto razonables
- overrides explícitos para recursos sensibles o workflows concretos
- crecimiento limpio sin mantenimiento infernal
