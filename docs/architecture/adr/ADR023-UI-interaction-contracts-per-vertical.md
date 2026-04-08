# ADR-0XY — UI Interaction Contracts per Vertical

## Status

Accepted

---

## Context

The application is structured in independent verticals:

* contacts
* addresses
* identifiers
* working_time
* contract
* labor_classification
* etc.

Each vertical:

* manages its own UI state
* performs operations (create/update/delete)
* interacts with backend services
* produces user feedback (success, errors, validation)

Without a formal contract, verticals tend to:

* implement feedback inconsistently
* mix local and global messages arbitrarily
* duplicate logic
* break UX predictability

A clear **interaction contract per vertical** is required.

---

## Decision

Each vertical MUST follow a standardized **UI interaction contract**:

> A vertical does not decide how feedback is shown.
> It only decides **what happened**.

Feedback rendering is delegated to the global system.

---

## Core Interaction Model

Every user action in a vertical follows this flow:

1. User action (click / submit)
2. Local UI state changes (loading, disabling inputs)
3. Backend call
4. Result handling:

   * success → publish global message
   * error → publish global message
   * validation → mark fields locally + optionally publish global
5. UI stabilization

---

## Standard Interaction Phases

### 1. Idle

* no pending operation
* inputs enabled

---

### 2. Processing

* triggered by user action
* UI MUST:

  * disable relevant inputs
  * show loading state (button spinner, etc.)
* MUST NOT show global message yet

---

### 3. Success

On successful operation:

* MUST call `GlobalMessageService.success(...)`
* MUST NOT render local success banner
* MAY:

  * reset form
  * refresh list/data
  * focus relevant UI area

---

### 4. Error

On operation failure:

* MUST call `GlobalMessageService.error(...)`
* MUST NOT render generic local error banners
* MUST:

  * re-enable inputs
* MAY:

  * highlight affected section
  * keep user input intact

---

### 5. Validation

Two types:

#### a) Inline validation (client-side)

* handled locally
* shown at field level
* does NOT go to global system

#### b) Backend/business validation

* MUST be published globally
* MAY also:

  * mark fields invalid
  * show inline hints

Example:

* “contactValue invalid” → global + field highlight
* “duplicate contact type” → global (not just local banner)

---

## Message Publishing Contract

Each vertical MUST use the global service:

```ts
messageService.success(...)
messageService.error(...)
messageService.warning(...)
```

A vertical MUST NOT:

* render global-like banners locally
* bypass the message system

---

## Section Awareness

When publishing messages, verticals SHOULD include:

* `sectionId`
* optional `fieldId`

This enables:

* navigation ("Go to section")
* scroll behavior
* contextual highlighting

---

## UI Responsibilities by Layer

### Vertical Component

Responsible for:

* capturing user interaction
* managing local UI state (loading, form state)
* invoking backend
* publishing message events

NOT responsible for:

* deciding how messages are rendered
* displaying global feedback

---

### Global Message System

Responsible for:

* rendering feedback
* animation
* stacking
* navigation
* lifecycle (auto-dismiss, sticky)

---

## Anti-Patterns (Forbidden)

* local success banners
* duplicated error messages (global + local)
* silent failures
* mixing rendering logic inside verticals
* inconsistent handling between verticals

---

## UX Consistency Rules

All verticals MUST behave consistently:

| Action            | Behavior               |
| ----------------- | ---------------------- |
| Create success    | Global success message |
| Update success    | Global success message |
| Delete success    | Global success message |
| Backend error     | Global error message   |
| Business conflict | Global error message   |
| Field invalid     | Local inline error     |

---

## Example — Contacts Vertical

### Create Contact

#### Success

* publish global success
* reset form
* refresh contact list

#### Error (duplicate type)

* publish global error
* keep form values
* optionally mark field

#### Validation (email format)

* local inline error only

---

## Consequences

### Positive

* consistent UX across all verticals
* clear separation of responsibilities
* easier maintenance
* Copilot can follow predictable patterns
* scalable to new verticals

---

### Trade-offs

* requires refactoring existing verticals
* stricter discipline in UI development
* less “freedom” inside components

---

## Evolution

Future enhancements may include:

* standardized helper hooks for verticals
* base abstract component for interaction handling
* unified error mapping from backend → UI
* analytics on user interaction failures

---

## Summary

Verticals do not control feedback presentation.

They only emit **interaction outcomes**.

The system controls how feedback is displayed.

---

## Golden Rule

> If a vertical performs an operation,
> it MUST publish the outcome to the global system.
