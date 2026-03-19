# B4RRHH — Vertical Language & Temporal Design Vocabulary (FULL)

## 1. Purpose
Define shared language for designing verticals:
- Avoid ad-hoc design
- Enable consistency
- Prepare future abstractions

---

## 2. Core Principles
- Vertical-first architecture
- Business keys in APIs (no technical IDs)
- Stable, business-oriented naming

Employee identity:
- ruleSystemCode
- employeeTypeCode
- employeeNumber

---

## 3. Core Concepts

### Vertical
A bounded domain slice with:
- business meaning
- identity
- rules
- API
- persistence

### Occurrence
A unit of data inside a vertical (often temporal).

### Timeline
Ordered set of occurrences for an employee.

### Projected Timeline
Timeline after applying an operation (before persistence).

### Coverage
Degree to which a timeline covers another (typically presence).

---

## 4. Vertical Types

### REFERENCE_CURRENT
- Not historized
- Current state only
- Example: contact

### FLEXIBLE_TIMELINE
- Historized
- No overlap
- No mandatory full coverage

### DISTRIBUTED_TIMELINE
- Multi-active
- Parallel occurrences allowed
- Example: cost_center

### STRONG_TIMELINE
- Historized
- Single active
- No overlap
- Full coverage required
- Needs replaceFromDate
- Examples: labor_classification, contract

---

## 5. Structural Properties

- historized: true/false
- occurrence_type: SINGLE/MULTIPLE
- simultaneous_occurrences: NONE/SINGLE_ACTIVE/MULTIPLE_ACTIVE
- lifecycle_strategy: NONE/CLOSE/DELETE
- delete_policy: FORBIDDEN/LOGICAL/PHYSICAL

---

## 6. Temporal Rules

- NO_OVERLAP
- SINGLE_ACTIVE
- MULTI_ACTIVE
- CONTAINED_IN_PRESENCE
- FULL_PRESENCE_COVERAGE
- OPEN_ENDED_PERIOD
- TIMELINE_PROJECTED_VALIDATION
- REPLACE_FROM_DATE
- TIMELINE_SPLIT

---

## 7. Catalog Validation Types

- SIMPLE_CATALOG_VALUE
- DATE_APPLICABLE_CATALOG_VALUE
- COMPOSITE_CATALOG_RELATION
- DATE_APPLICABLE_CATALOG_RELATION

---

## 8. Functional Identity

Types:
- employee + code
- employee + startDate
- employee + functional number

Rules:
- no technical IDs in API
- identity fields are immutable

---

## 9. Business Operations

- create
- update
- close
- replaceFromDate

Future:
- split
- merge
- normalizeTimeline

---

## 10. Emerging Patterns

### Strong Timeline Pattern
- single-active
- full coverage
- replaceFromDate
- timeline validation

### Distributed Timeline Pattern
- multi-active
- aggregate rules
- percentage-based

---

## 11. Design Checklist

- vertical type defined
- identity defined
- temporal rules explicit
- presence relation clear
- catalog type defined
- operations defined

---

## 12. Abstraction Rule

Only abstract when:
- pattern repeated >= 2-3 times
- variation is understood

Allowed early:
- date utils
- range helpers
- coverage validation

Avoid:
- generic engines
- metadata-driven frameworks

---

## 13. Final Rule

A vertical is defined by:
- type
- identity
- temporal rules
- catalog validation
- operations

Not by:
- table
- DTO
- controller
