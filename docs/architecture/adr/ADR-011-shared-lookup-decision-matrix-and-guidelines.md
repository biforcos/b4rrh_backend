# B4RRHH — Matriz de adopción del patrón shared lookup y guía de diseño

Fecha: 2026-03-21

## 1. Objetivo

Este documento fija dos cosas:

1. una **matriz práctica** para decidir qué verticales de `employee` deben adoptar el patrón shared de lookup por business key;
2. una **guía de diseño estable** para que tanto una persona como Copilot mantengan la misma disciplina al crear o refactorizar verticales futuras.

El contexto actual es que ya existe un soporte shared mínimo de persistencia con `EmployeeBusinessKeyLookupSupport` y `EmployeeOwnedLookupSupport`, y ya se está usando en `contact`, `identifier` y `address` para resolver employee por business key y mapear a su contexto sin duplicar plumbing técnico. fileciteturn12file4 fileciteturn12file7 fileciteturn12file3 fileciteturn12file0 fileciteturn12file2

---

## 2. Regla madre

En B4RRHH, el código debe organizarse **primero por vertical y luego por capas**, y las APIs públicas deben trabajar con **business keys**, no con IDs técnicos. Los IDs técnicos deben quedarse en persistencia. fileciteturn12file15 fileciteturn12file16

Además, `employee.shared` sólo debe contener piezas **realmente transversales y técnicas**. No debe convertirse en un cajón de semántica de negocio. fileciteturn12file15

Traducido a esta decisión concreta:

- **sí** a helpers pequeños y explícitos para lookup transversal repetido;
- **no** a repositorios universales, engines genéricos o shared con vocabulario funcional de un vertical.

---

## 3. Qué patrón se considera ya consolidado

A día de hoy, el patrón compartido que se considera válido es éste:

1. resolver `EmployeeEntity` por business key (`ruleSystemCode`, `employeeTypeCode`, `employeeNumber`);
2. resolver opcionalmente la variante con lock (`for update`);
3. delegar en una lambda o función local del vertical;
4. mantener el mapping a `EmployeeXContext` o la excepción del vertical en el propio vertical.

Ese patrón está ya expresado en:

- `EmployeeBusinessKeyLookupSupport`, que delega en `SpringDataEmployeeRepository.findByBusinessKey(...)` y `findByBusinessKeyForUpdate(...)`; fileciteturn12file4 fileciteturn12file18
- `EmployeeOwnedLookupSupport`, que compone el lookup del employee con una función `ownedLookup`, tanto en modo `Optional` como en modo `OrThrow`; fileciteturn12file7 fileciteturn12file12
- `EmployeeContactLookupAdapter`, `EmployeeIdentifierLookupAdapter` y `EmployeeAddressLookupAdapter`, que ya usan ese soporte compartido y hacen sólo el mapping explícito a su contexto. fileciteturn12file3 fileciteturn12file0 fileciteturn12file2

---

## 4. Matriz de adopción por vertical

### 4.1 Resumen ejecutivo

| Vertical | Estado recomendado | Motivo corto |
|---|---|---|
| `contact` | Ya adoptado | lookup de owner puro y mapping simple |
| `identifier` | Ya adoptado | lookup de owner puro y mapping simple |
| `address` | Ya adoptado | lookup de owner puro y mapping simple; semántica temporal queda fuera |
| `presence` | Candidato fuerte siguiente | patrón de employee-context probablemente muy parecido |
| `workcenter` | Candidato medio | posible encaje para employee-context, pero revisar mezcla con validaciones de presencia |
| `cost_center` | Candidato medio | posible encaje para employee-context, pero revisar mezcla con temporalidad y porcentaje |
| `contract` | Esperar | vertical más cargado de timeline y replace semantics |
| `labor_classification` | Esperar | vertical más cargado de timeline, cobertura y relaciones |
| `journey` | No aplicar este patrón | es vertical de lectura/proyección, no de ownership lookup estándar |
| `employee` raíz | No aplica | es el owner, no un child vertical |

### 4.2 Lectura detallada

#### `contact` — Ya adoptado

Encaja perfectamente porque el adapter sólo resuelve employee por business key y mapea a `EmployeeContactContext`. No se mete negocio del vertical en shared. fileciteturn12file3

#### `identifier` — Ya adoptado

Mismo caso que `contact`: lookup puro del owner y mapping local. fileciteturn12file0

#### `address` — Ya adoptado

El refactor ha eliminado `EntityManager` y SQL nativo del adapter de lookup y lo ha alineado con el mismo patrón de `contact` e `identifier`. La temporalidad de `address` sigue viviendo fuera de este helper. fileciteturn12file11 fileciteturn12file2 fileciteturn12file14

#### `presence` — Candidato fuerte siguiente

Por estructura, es muy probable que tenga el mismo patrón de resolver employee owner y construir `EmployeePresenceContext`. Si el adapter se parece a `contact`/`identifier`/`address`, debería entrar. La condición es no mezclar ahí reglas como overlap, presencia activa o cierre. Además, en la arquitectura objetivo `presence` debe tender al mismo modelo vertical-first que `contact`. fileciteturn12file17

#### `workcenter` — Candidato medio

Puede encajar si existe un `EmployeeWorkCenterLookupAdapter` que sólo resuelva contexto de employee. Debe quedarse fuera cualquier lógica de cobertura respecto a presence, gaps o consistencia. Si el adapter mezcla lookup y validación de cobertura, primero hay que separarlo.

#### `cost_center` — Candidato medio

Misma idea que `workcenter`. Puede encajar para la parte de owner lookup, pero sólo si la lógica de asignación, porcentaje y restricciones temporales permanece fuera.

#### `contract` — Esperar

Aquí el riesgo de contaminar el refactor con lógica temporal es alto: `replaceFromDate`, cobertura de presence, subtype relation, cierre, update con semántica fuerte. Mejor no meterlo aún en esta ola.

#### `labor_classification` — Esperar

Caso parecido a `contract`: reglas temporales más ricas y mayor probabilidad de que lookup y negocio estén más acoplados.

#### `journey` — No aplicar

`journey` es un vertical de lectura/proyección. Su problema no es ownership lookup de un child resource estándar, sino composición de tracks y eventos. Este patrón no le aporta gran cosa.

---

## 5. Regla de decisión rápida

Una vertical **debe entrar** en el patrón shared si se cumplen estas cinco:

1. el adapter necesita resolver employee por business key;
2. la parte repetida es claramente técnica;
3. el resultado es un contexto o un owned lookup simple;
4. el shared no necesita aprender vocabulario del vertical;
5. el adapter queda más legible después del cambio.

Una vertical **no debe entrar todavía** si pasa cualquiera de estas cuatro:

1. el refactor arrastra reglas temporales o de negocio;
2. obliga a meter semántica funcional del vertical en `shared`;
3. hace el código más mágico o más difícil de depurar;
4. la identidad funcional del recurso hijo todavía no está del todo clara.

---

## 6. Qué sí puede vivir en `employee.shared`

### Sí

- lookup de `EmployeeEntity` por business key; fileciteturn12file4
- variante con lock (`for update`); fileciteturn12file4
- composición técnica `employee -> ownedLookup`; fileciteturn12file7
- utilidades técnicas pequeñas y explícitas que se repiten igual en varios verticales.

### No

- `contactTypeCode`, `identifierTypeCode`, `addressTypeCode`, `addressNumber`, etc.;
- validaciones de catálogo del vertical;
- lógica de overlap, coverage, split, replace, close o correct;
- excepciones de dominio genéricas que sustituyan a las del vertical;
- repositorios universales tipo `EmployeeOwnedRepository<T, K>`.

---

## 7. Convenciones de diseño que deben quedar escritas

### 7.1 Identidad

La identidad pública siempre debe expresarse con business keys. En `employee`, eso significa al menos:

- `ruleSystemCode`
- `employeeTypeCode`
- `employeeNumber` fileciteturn12file15 fileciteturn12file16

Los IDs técnicos sólo deben vivir en persistencia. fileciteturn12file15

### 7.2 Organización del código

El patrón objetivo sigue siendo:

- `com.b4rrhh.<bounded-context>.<vertical>.application`
- `com.b4rrhh.<bounded-context>.<vertical>.domain`
- `com.b4rrhh.<bounded-context>.<vertical>.infrastructure` fileciteturn12file15

### 7.3 Regla de abstracción

Extraer helper sólo cuando:

- el patrón ya se repite;
- la variación está entendida;
- la abstracción hace el código más simple, no más listo.

### 7.4 Mapping

El mapping de `EmployeeEntity -> EmployeeXContext` debe seguir siendo **local al adapter del vertical**, como ya pasa en `contact`, `identifier` y `address`. fileciteturn12file3 fileciteturn12file0 fileciteturn12file2

### 7.5 Tests mínimos al introducir una vertical en este patrón

Cada adapter que adopte el patrón debe tener al menos:

- caso feliz en lookup normal;
- caso feliz en lookup for-update;
- `Optional.empty()` cuando no existe employee en lookup normal;
- `Optional.empty()` cuando no existe employee en lookup for-update.

Eso ya está aplicado en `address`, y de forma equivalente en `contact` e `identifier`. fileciteturn12file5 fileciteturn12file8 fileciteturn12file9

---

## 8. Guardarraíles para diseñar verticales nuevas

Cuando nazca una vertical nueva bajo `employee`, aplicar este checklist antes de escribir código:

1. **Identidad funcional**: ¿cuál es la business key pública del recurso?
2. **Ownership**: ¿ese recurso cuelga de employee por business key?
3. **Naturaleza**: ¿es `SLOT`, `TEMPORAL_APPEND_CLOSE`, workflow u otra familia? Esto es importante también para frontend y UX. En V1, por ejemplo, `contact` e `identifier` se tratan como `SLOT` y `address` como `TEMPORAL_APPEND_CLOSE`. fileciteturn12file19
4. **Lookup técnico**: ¿hay un adapter de contexto que sólo resuelve employee? Si sí, debe usar el patrón shared.
5. **Reglas de negocio**: ¿qué debe quedarse fuera de shared sí o sí?
6. **DTOs y endpoints**: ¿están formulados por business keys y no por IDs técnicos?
7. **Tests**: ¿se están probando ownership, duplicados, validación y constraints? fileciteturn12file15

---

## 9. Prompt base recomendado para Copilot

### Uso

Este bloque sirve como cabecera de contexto para cualquier refactor o implementación futura en verticales `employee`.

```text
You are working in the B4RRHH project.

Mandatory project rules:
- Architecture is vertical-first inside each bounded context.
- Public APIs must use functional business keys, never technical IDs.
- Technical IDs must remain inside persistence.
- employee.shared may contain only truly transversal technical support.
- Do NOT move vertical-specific business rules into shared.
- Prefer small explicit helpers over generic frameworks.
- Keep mappings local to the vertical adapter when they express vertical context.
- Introduce abstractions only when the pattern is already repeated and variation is understood.

Current shared lookup pattern already accepted:
- EmployeeBusinessKeyLookupSupport resolves EmployeeEntity by business key, including for-update lookup.
- EmployeeOwnedLookupSupport composes employee lookup with a local owned lookup function.
- contact, identifier and address already use this pattern for employee-context lookup.

Design guardrails:
- Do not create universal repositories.
- Do not create generic domain exceptions that replace vertical exceptions.
- Do not move overlap, temporal, coverage, replace, close or correction semantics into shared.
- Keep EmployeeEntity -> EmployeeXContext mapping local to the vertical adapter.
- Any new vertical must first define its functional identity and maintenance model.

When deciding whether a vertical should adopt the shared lookup pattern, use this rule:
Adopt it only if the duplicated code is clearly technical owner lookup and the result is simpler after refactoring.
```

---

## 10. Decisión operativa recomendada desde hoy

Orden sugerido para próximas revisiones:

1. `presence`
2. `workcenter`
3. `cost_center`
4. parar y reevaluar
5. dejar `contract` y `labor_classification` para una discusión separada

La razón es simple: conviene seguir capturando el patrón donde el beneficio es alto y el riesgo semántico es bajo, y frenar antes de entrar en verticales donde la lógica temporal fuerte pueda contaminar la abstracción.

---

## 11. Resumen ejecutivo

- El patrón shared de lookup ya está consolidado para `contact`, `identifier` y `address`. fileciteturn12file3 fileciteturn12file0 fileciteturn12file2
- El patrón correcto es pequeño: resolver employee por business key, componer una lambda local y dejar mapping/excepciones en el vertical. fileciteturn12file4 fileciteturn12file7
- `presence` es el siguiente candidato natural.
- `workcenter` y `cost_center` son candidatos posibles, pero sólo para la parte de owner lookup.
- `contract` y `labor_classification` deben esperar.
- Este documento debe usarse como criterio de diseño y como prólogo de prompts para Copilot.
