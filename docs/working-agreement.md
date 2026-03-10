# B4RRHH — Working Agreement

Propuesta ligera de trabajo para seguir creciendo sin perder lateralidades.

## Regla base

Cuando aparezca algo nuevo, clasificarlo en una de estas tres categorías:

### 1. Idea o preocupación
Va a `backlog.md`

Ejemplos:
- “igual deberíamos meter `employee_type`”
- “hay que probar varios `rule_systems`”
- “tenemos un 500 raro”

### 2. Decisión estructural
Va a `architecture-decisions.md`

Ejemplos:
- la identidad lógica del empleado cambia
- se adopta una convención de claves
- se decide que ciertas validaciones serán genéricas

### 3. Cambio efectivo del modelo
Va al catálogo o documento fuente de dominio correspondiente

Ejemplos:
- cambia la business key
- un recurso pasa de `DELETE` a `CLOSE`
- se añade `employee.leave`

---

## Flujo de trabajo recomendado contigo

1. Durante la conversación, cuando salga algo lateral, lo capturamos enseguida.
2. Si es una simple idea, la añadimos al backlog.
3. Si ya huele a decisión de arquitectura, la convertimos en ADR.
4. Si la decisión afecta al modelo, luego actualizamos el catálogo.

---

## Qué no hace falta todavía

No hace falta montar ahora mismo un kanban completo si va a meter más fricción que valor.

## Cuándo sí dar el salto a algo más formal

Tiene sentido pasar a GitHub Projects, Notion, Linear o similar si empieza a ocurrir alguna de estas cosas:

- varias personas tocando a la vez el mismo backlog
- necesidad de priorizar por sprint
- dependencia entre tareas
- seguimiento de bugs y releases
- necesidad de enlazar tickets con PRs de forma sistemática

---

## Limitación práctica

Yo no tengo acceso directo a tu repositorio GitHub salvo que me pegues contenido aquí o uses herramientas externas concretas con acceso. Así que la forma realista de trabajar es:

- tú mantienes estos documentos en el repo
- yo te ayudo a redactarlos, reorganizarlos y evolucionarlos aquí
- cuando haga falta, me pegas el contenido actual y lo actualizamos juntos
