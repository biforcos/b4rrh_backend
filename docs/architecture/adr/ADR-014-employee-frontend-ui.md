# ADR — Employee Frontend Section System and Visual Identity

## 1. Objetivo
Definir un sistema visual coherente y reutilizable para la ficha de empleado basado en verticales.

## 2. Principios
- La ficha es composición de verticales
- Consistencia visual transversal
- Backend-driven UI
- No formularios monolíticos

## 3. Unidad base: Section Shell
Componente base que define:
- Header (título + acciones)
- Body (contenido)
- Footer (estado: loading/error/success)

## 4. Maintenance modes → UI
- SLOT → lista editable
- TEMPORAL_APPEND_CLOSE → histórico con activo
- WORKFLOW → acciones guiadas
- READONLY → solo lectura

## 5. Contratos base

### SectionUiState
- mode
- dirty
- busy
- errorMessage
- successMessage

### SectionCapabilities
- canCreate
- canEdit
- canDelete
- canClose
- canCorrect
- canLaunchWorkflow

## 6. Componentes base
- employee-section-shell
- editable-slot-section
- temporal-section

## 7. Tokens visuales
- spacing
- border radius
- colores base

## 8. Reglas Copilot
- No crear componentes genéricos universales
- No mezclar lógica de negocio en UI
- Reutilizar shell y contratos

## 9. Estrategia
1. Implementar shell
2. Aplicar a contacts
3. Reutilizar en identifiers
4. Extender a temporales
