# ADR-0XX — Global Message & Feedback Policy

## Status

Accepted

## Context

The application has evolved into a complex, multi-vertical UI (employee, contacts, addresses, working_time, etc.) with:

* independent sections
* multiple interaction points
* backend-driven validations and business rules

Previously, user feedback (errors, success, warnings) was:

* duplicated across components
* inconsistently displayed
* sometimes invisible or easy to miss
* tightly coupled to local UI sections

This created confusion:

* users did not know where to look for feedback
* messages lacked hierarchy
* visual noise increased with complexity

A unified, system-level feedback mechanism is required.

---

## Decision

Introduce a **Global Floating Message System** as the single source of truth for application feedback.

This system is:

* **global** → not tied to any specific section
* **floating** → overlays UI, does not affect layout
* **centralized** → managed via a shared service
* **hierarchical** → distinguishes between message types and scopes

---

## Core Principles

### 1. Single Source of Truth

All operation-level feedback MUST go through the global message system.

No duplicated messages across components.

---

### 2. Non-Intrusive Overlay

Messages:

* MUST NOT modify page layout
* MUST NOT push content down
* MUST float above the UI

---

### 3. Clear Separation of Concerns

| Message Type            | Location   |
| ----------------------- | ---------- |
| Operation success       | Global     |
| Backend errors          | Global     |
| Business rule conflicts | Global     |
| Submit errors           | Global     |
| Warnings                | Global     |
| Inline field validation | Local only |

---

### 4. Predictable User Experience

The user MUST always know:

> “If something important happened, I look at the global message layer.”

---

## Message Types

### Success

* lightweight
* auto-dismiss
* visually subtle
* includes visible timeout indicator

### Error

* sticky
* requires attention
* visually stronger but not aggressive
* may include navigation to affected section

### Warning

* visible but less dominant than error
* may allow continuation

### Info

* optional
* low priority

---

## Behavior Rules

### Entry / Exit

Messages MUST have:

* smooth entry animation (fade + slight movement)
* smooth exit animation
* no abrupt appearance/disappearance

---

### Auto-dismiss

* success messages auto-dismiss with visible progress
* errors remain until dismissed or resolved

---

### Stacking

* limit visible messages (max 2–3)
* group or summarize if necessary

---

### Navigation

If a message is linked to a section:

* user can navigate via “Go to section”
* system may:

  * activate tab
  * scroll to section
  * highlight briefly

---

## Publication Rules

### MUST publish to global system

* create/update/delete operations
* backend validation errors
* business conflicts
* workflow errors (hire, terminate, etc.)

### MUST NOT remain only local

Examples:

* “Ya existe un contacto para ese tipo”
* “Invalid working_time configuration”

These MUST be global.

---

### Local-only feedback

Allowed only for:

* field validation while typing
* input-level hints
* invalid/touched states

---

## Anti-Patterns (Forbidden)

* duplicated messages (global + local banner)
* full-width banners inside sections for operation results
* messages that modify layout flow
* silent failures (no visible feedback)

---

## Implementation

### GlobalMessageService

Responsible for:

* publishing messages
* managing lifecycle
* deduplication
* stacking rules

### app-global-message-rail

Responsible for:

* rendering floating overlay
* animations
* user interaction
* navigation hooks

---

## Consequences

### Positive

* consistent UX
* predictable feedback model
* reduced duplication
* scalable across verticals
* Copilot-friendly (clear rules)

### Trade-offs

* requires refactoring of existing components
* forces discipline in message publishing
* initial overhead to standardize

---

## Future Improvements

* grouping by section/vertical
* message prioritization
* accessibility enhancements (ARIA/live regions)
* analytics on user interactions with messages

---

## Summary

Feedback is no longer a UI detail.

It is a **system-level capability**.

All meaningful application feedback must be:

> centralized, visible, predictable, and non-intrusive.
