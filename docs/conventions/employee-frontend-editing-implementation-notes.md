# Employee Frontend Editing — Implementation Notes (B4RRHH)

## Estado
Working notes complementarias al ADR de edición frontend.

---

## 1. Objetivo

Aterrizar el ADR **Employee Frontend Editing Pattern by Vertical Maintenance Mode**
a decisiones concretas de implementación frontend, sin introducir todavía código
de negocio ni prompts de Copilot demasiado cerrados.

Estas notas sirven para:

- unificar experiencia de usuario
- fijar naming y estructura
- evitar que Copilot improvise
- separar decisiones de producto, UI y wiring técnico

---

## 2. Principios heredados del proyecto

Estas notas asumen y respetan las siguientes reglas del proyecto:

- Arquitectura frontend validada: `client → mapper → gateway → store → UI`
- Backend manda
- APIs por business keys
- Vertical-first
- El frontend no inventa semántica de negocio
- La ficha de empleado es composición de verticales, no un formulario monolítico

---

## 3. Unidad base de UX: Employee Section

Cada vertical visible dentro de la ficha se representa como una **section**.

### Anatomía estándar

- `header`
  - título
  - subtítulo opcional
  - acciones de bloque
- `body`
  - lectura
  - edición
  - alta
  - confirmación
- `footer`
  - loading
  - error
  - success discreto

### Objetivo

Todas las verticales deben parecer parte de la misma familia visual, aunque su
comportamiento interno no sea idéntico.

**Principio:**
igual por fuera, honesta por dentro.

---

## 4. Maintenance Modes

La UX no se organiza por “tipo de formulario”, sino por **familia de comportamiento**
del vertical.

### 4.1 SLOT
Para verticales tipo slot por tipo.

Uso inicial:
- contacts
- identifiers

Comportamiento:
- alta
- edición
- eliminación
- edición centrada en fila

### 4.2 TEMPORAL_APPEND_CLOSE
Para verticales historizados donde el cambio funcional normal se modela mediante:
- alta de nueva ocurrencia
- cierre de ocurrencia vigente

Uso inicial:
- addresses

Comportamiento inicial V1:
- añadir
- cerrar
- sin corrección inline, salvo soporte explícito de backend

### 4.3 WORKFLOW
Para verticales cuya modificación requiere acciones de negocio y no CRUD directo.

Uso futuro:
- presence
- contract
- labor classification

### 4.4 READONLY
Para verticales todavía no abiertas a mantenimiento.

---

## 5. Contrato visual mínimo por sección

Cada sección debe poder responder a estas preguntas:

- ¿qué estoy viendo?
- ¿puedo actuar?
- ¿qué acción está en curso?
- ¿qué ha fallado?
- ¿qué se ha guardado?

### Acciones visibles honestas

No usar “Editar” como verbo universal.

Usar la acción real:
- Editar
- Añadir
- Eliminar
- Cerrar
- Corregir
- Lanzar workflow

---

## 6. Contrato de estado UI por sección

Se recomienda un estado UI base común por sección.

```ts
export type SectionMode =
  | 'view'
  | 'editing'
  | 'creating'
  | 'confirming'
  | 'submitting'
  | 'error';

export interface SectionUiState {
  mode: SectionMode;
  dirty: boolean;
  busy: boolean;
  errorMessage: string | null;
  successMessage: string | null;
}
```

Este estado base puede ampliarse por vertical.

### Ejemplo: contacts

```ts
export interface ContactSectionUiState extends SectionUiState {
  editingContactTypeCode: string | null;
  deletingContactTypeCode: string | null;
  draftContact: EditableContactDraft | null;
}
```

### Ejemplo: addresses

```ts
export interface AddressSectionUiState extends SectionUiState {
  closingAddressNumber: number | null;
  draftAddress: EditableAddressDraft | null;
}
```

---

## 7. Separación clave: data remota vs estado efímero de edición

No mezclar:

- datos leídos del backend
- estado local de edición

### Regla

Los datos canónicos viven en store / load model.

El estado efímero de UI vive en el contenedor de sección, salvo necesidad clara
de elevarlo.

### Store
Debe manejar:
- carga
- refresh
- mutation loading
- mutation error
- mutation success

### Componente de sección
Debe manejar:
- fila en edición
- draft actual
- confirm abierto/cerrado
- dirty state
- estado de formulario

---

## 8. Regla V1 de concurrencia

### Regla recomendada
Solo una operación activa por sección.

No permitir simultáneamente dentro de la misma sección:
- editar una fila
- crear otra
- borrar una tercera

### Regla opcional V1
Preferiblemente, una única sección en edición en toda la ficha.

No es requisito técnico absoluto, pero simplifica:
- foco del usuario
- manejo de errores
- consistencia de UX

---

## 9. Persistencia y sincronización

### Regla principal
El backend es la fuente de verdad.

### Secuencia estándar
1. usuario lanza acción
2. sección entra en `submitting`
3. gateway ejecuta operación
4. en éxito:
   - refresh del bloque o de la ficha
   - reset de estado local
   - success discreto
5. en error:
   - mantener contexto
   - mostrar error local

### Regla V1
No implementar lógica compleja de actualización optimista.

Mutación → refresh.

---

## 10. Contrato de acciones recomendado

Se recomienda naming uniforme y explícito.

### Acciones base

- `startCreate()`
- `startEdit(key)`
- `requestDelete(key)`
- `requestClose(key)`
- `cancel()`
- `submitCreate()`
- `submitEdit()`
- `confirmDelete()`
- `confirmClose()`

### Evitar
- `handleAction`
- `saveItem`
- `processForm`
- `doSubmit`

---

## 11. Capabilities por vertical

Cada vertical debe declarar sus capacidades de mantenimiento.

```ts
export interface SectionCapabilities {
  canCreate: boolean;
  canEdit: boolean;
  canDelete: boolean;
  canClose: boolean;
  canCorrect: boolean;
  canLaunchWorkflow: boolean;
}
```

### Contacts

```ts
{
  canCreate: true,
  canEdit: true,
  canDelete: true,
  canClose: false,
  canCorrect: false,
  canLaunchWorkflow: false
}
```

### Identifiers

```ts
{
  canCreate: true,
  canEdit: true,
  canDelete: true,
  canClose: false,
  canCorrect: false,
  canLaunchWorkflow: false
}
```

### Addresses V1

```ts
{
  canCreate: true,
  canEdit: false,
  canDelete: false,
  canClose: true,
  canCorrect: false,
  canLaunchWorkflow: false
}
```

---

## 12. Familia inicial de componentes

No construir un supercomponente universal.

Se recomienda una familia corta de componentes.

### 12.1 `employee-section-shell`
Responsabilidad:
- layout común
- header
- acciones de bloque
- footer de estado

No contiene semántica de negocio.

### 12.2 `editable-slot-section`
Uso:
- contacts
- identifiers

Responsabilidad:
- lista de filas
- edición de una fila
- alta de fila
- borrado con confirmación

### 12.3 `temporal-section`
Uso:
- addresses

Responsabilidad:
- lista histórica
- destacar ocurrencia activa
- alta de nueva ocurrencia
- cierre de ocurrencia vigente

### 12.4 `workflow-section`
No implementar todavía, pero reservar como patrón futuro.

---

## 13. Naming Angular recomendado

### Containers
- `employee-contact-section.component.ts`
- `employee-identifier-section.component.ts`
- `employee-address-section.component.ts`

### Presentational / shared
- `employee-section-shell.component.ts`
- `editable-slot-section.component.ts`
- `temporal-section.component.ts`

### Models
- `section-ui-state.model.ts`
- `section-capabilities.model.ts`
- `editable-contact-draft.model.ts`
- `editable-identifier-draft.model.ts`
- `editable-address-draft.model.ts`

### Gateways
- `employee-contact.gateway.ts`
- `employee-identifier.gateway.ts`
- `employee-address.gateway.ts`

### Mappers
- `employee-contact-edit.mapper.ts`
- `employee-identifier-edit.mapper.ts`
- `employee-address-edit.mapper.ts`

---

## 14. Decisiones específicas V1 por vertical

### 14.1 Contacts
Patrón:
- SLOT

Operaciones:
- añadir
- editar valor
- eliminar

Reglas UI:
- `contactTypeCode` editable solo en alta
- `contactValue` editable en alta y edición
- una sola fila en edición a la vez

### 14.2 Identifiers
Patrón:
- SLOT

Operaciones:
- añadir
- editar valor
- eliminar

Reglas UI:
- `identifierTypeCode` editable solo en alta
- valor editable en alta y edición
- una sola fila en edición a la vez

### 14.3 Addresses
Patrón:
- TEMPORAL_APPEND_CLOSE

Operaciones V1:
- añadir
- cerrar

Reglas UI:
- no hay edición inline de ocurrencia existente
- mostrar claramente cuál está activa
- confirmación ligera para cierre
- si en futuro backend soporta `correct`, se ampliará el patrón

---

## 15. Reglas de diseño para Copilot

Copilot no debe:

- convertir toda la ficha en un formulario global
- crear un form builder genérico
- unificar direcciones con contactos forzando el mismo comportamiento
- meter semántica de dominio en componentes presentacionales
- inventar una operación `correct` si backend no la tiene
- guardar solo estado local sin refresh posterior

Copilot sí debe:

- reutilizar shell visual
- mantener estados locales simples
- dejar la semántica en gateway/store/containers
- nombrar acciones por su verbo real
- mantener separación entre lectura y edición

---

## 16. Orden recomendado de implementación

### Fase 1
Base común:
- `employee-section-shell`
- modelos de estado comunes
- contratos de acciones
- helpers UI ligeros

### Fase 2
Slot pattern:
- contacts
- identifiers

### Fase 3
Temporal pattern:
- addresses

### Fase 4
Refinamiento:
- mensajes
- estados
- consistencia visual
- tests básicos de store

---

## 17. Tests mínimos recomendados

### Store / state
- mutation success refresca datos
- mutation error conserva contexto y expone error
- reset correcto de estado tras cancel
- no se inicia nueva operación incompatible si ya hay una activa

### UI
No obsesionarse con cobertura exhaustiva en V1.
Priorizar:
- happy path
- cancel
- error visible

---

## 18. Decisión práctica final

Para V1:

- **contacts** e **identifiers** comparten patrón y pueden reutilizar la misma familia de componentes
- **addresses** tendrá sección específica
- la consistencia vendrá del shell y del contrato de estados/acciones, no de forzar un único componente universal

---

## 19. Resumen ejecutivo

La edición frontend de empleado en B4RRHH debe implementarse como una composición
de secciones autónomas.

Cada sección:
- sigue un maintenance mode
- tiene estado UI propio
- ejecuta operaciones explícitas
- refresca desde backend tras mutación

La unificación debe producirse en:
- anatomía visual
- naming
- estados
- contrato de acciones

No en una abstracción genérica que borre la semántica real de cada vertical.
