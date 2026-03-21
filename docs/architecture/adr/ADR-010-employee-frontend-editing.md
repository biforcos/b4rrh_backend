# ADR — Employee Frontend Editing Pattern by Vertical Maintenance Mode

## Status
PROPOSED

---

## Contexto

El frontend de B4RRHH ya permite visualizar la ficha de empleado basada en verticales independientes:

- Arquitectura: client → mapper → gateway → store → UI
- Backend basado en business keys (no IDs técnicos)
- Verticales con distinta naturaleza:
  - Datos simples (contactos, identificadores)
  - Datos temporales (direcciones)
  - Datos complejos (presence, contracts)

Actualmente la UI es read-only y se requiere introducir edición.

---

## Problema

Diseñar un patrón de edición que:
- Sea consistente
- Respete el dominio
- Evite sobreingeniería
- Escale a futuro

Además, en verticales temporales, debe distinguirse entre:
- cambio funcional real
- corrección administrativa de una ocurrencia mal capturada

Sin esa distinción, el frontend puede acabar representando errores de captura como si fueran eventos reales de negocio.

---

## Decisión

Se adopta el patrón:

## Editable Resource Block by Maintenance Mode

---

## 1. Unidad de interacción: BLOQUE

Cada vertical se representa como un bloque autónomo.

Reglas:
- Independiente
- Con su propio estado
- Sin edición global de ficha

---

## 2. Maintenance Mode

Cada vertical define su modo de mantenimiento:

### SLOT
Para verticales tipo lista simple o “slot por tipo”.

Uso típico:
- contactos
- identificadores

Características:
- Alta
- Edición
- Eliminación
- Operación normalmente centrada en una fila

---

### TEMPORAL_APPEND_CLOSE
Para verticales historizados cuyo cambio funcional normal se expresa mediante:
- alta de una nueva ocurrencia
- cierre de la ocurrencia vigente

Uso típico:
- direcciones

Características:
- No modela un update directo como cambio normal
- El histórico se preserva por append + close
- La semántica principal es temporal, no CRUD clásico

#### Nota importante
TEMPORAL_APPEND_CLOSE **no implica** que toda modificación de una ocurrencia deba resolverse siempre con “cerrar y crear otra”.

Debe distinguirse entre:

##### a) Cambio funcional real
Ejemplos:
- el empleado se muda
- cambia la dirección efectiva desde una fecha
- hay un reemplazo de la ocurrencia vigente

En estos casos:
- add / append
- close
- eventualmente replace

##### b) Corrección administrativa
Ejemplos:
- calle mal escrita
- portal erróneo
- país o código postal mal informado
- error de captura reciente

En estos casos, cerrar y recrear puede:
- ensuciar el histórico
- generar falsos eventos de negocio
- introducir ruido funcional o de auditoría

Por tanto:

- TEMPORAL_APPEND_CLOSE **por defecto no incluye corrección**
- pero **puede ampliarse** con una operación explícita de `correct` si el dominio y el backend la soportan

#### Regla de frontend
El frontend **no inventará** semánticas de corrección si el backend no expone una operación compatible.

---

### WORKFLOW
Para verticales cuya modificación requiere acciones de negocio, no CRUD directo.

Uso típico:
- presence
- contracts
- labor classification

Características:
- No se presentan como edición genérica
- Se accionan mediante flujos explícitos
- Ejemplos futuros:
  - hire
  - termination
  - rehire
  - replace from date

---

### READONLY
Para verticales puramente informativos o todavía no abiertos a mantenimiento.

---

## 3. Modelo de interacción

Cada bloque tiene:

- displayMode: read | edit | create | busy | error
- maintenanceMode
- supportedActions

Ejemplo:
- contact → maintenanceMode = SLOT
- identifier → maintenanceMode = SLOT
- address → maintenanceMode = TEMPORAL_APPEND_CLOSE

---

## 4. UX

Reglas generales:
- Edición por bloque
- Una sola sesión activa por bloque
- Preferiblemente una única sesión de edición en toda la ficha en V1
- Operaciones por fila cuando aplique
- Feedback simple, discreto y local al bloque

### Principio de honestidad UX
La UI debe mostrar la acción real soportada por el dominio:
- Editar
- Añadir
- Eliminar
- Cerrar
- Corregir
- Lanzar workflow

No debe usarse “Editar” como verbo universal si la semántica real es otra.

---

## 5. Persistencia

- Backend es la fuente de verdad
- Tras mutación exitosa:
  - refresh del bloque o de la ficha
- No se introduce lógica rica de reconstrucción local si no aporta valor claro

---

## 6. Consecuencias

### Positivas
- Consistencia visual
- Respeto a la semántica del dominio
- Escalabilidad hacia workflows
- Permite distinguir entre cambio funcional y corrección administrativa
- Evita forzar CRUD donde no encaja

### Negativas
- Más componentes específicos por tipo de bloque
- Menos reutilización artificial
- Algunos verticales requerirán discusión explícita sobre si soportan `correct`

---

## 7. Alternativas descartadas

### Form builder genérico
Rechazado por pérdida de semántica y exceso de abstracción.

### Edición global de ficha
Rechazado por complejidad, peor control de estado y mal encaje con un dominio verticalizado.

### Tratar todos los temporales como append/close puro
Rechazado como regla universal porque puede convertir errores administrativos en falsos cambios funcionales.

---

## 8. Aplicación inicial

- Contactos → SLOT
- Identificadores → SLOT
- Direcciones → TEMPORAL_APPEND_CLOSE

### Decisión específica para V1 de direcciones
En V1:
- Añadir nueva dirección
- Cerrar dirección existente
- Sin corrección inline de ocurrencia existente, salvo que backend exponga operación específica

Esto se considera una decisión de alcance, no una verdad permanente del patrón.

---

## 9. Futuro

Posibles evoluciones:
- Introducción formal de operación `correct` en verticales temporales
- Integración de workflows
- Refinamiento de `supportedActions` por vertical
- ADR complementario si se consolida distinción explícita entre:
  - correction
  - replacement
  - close

---

## 10. Resumen ejecutivo

El frontend de empleado no se modelará como un gran formulario, sino como una composición de bloques autónomos.

Cada bloque declara un maintenance mode.

La edición no se unifica por “tipo de formulario”, sino por “familia de comportamiento” del vertical.

Para verticales temporales:
- el cambio funcional normal puede expresarse como append/close
- pero la corrección administrativa no debe confundirse automáticamente con un cambio de negocio

El frontend respetará siempre la semántica realmente soportada por backend.
