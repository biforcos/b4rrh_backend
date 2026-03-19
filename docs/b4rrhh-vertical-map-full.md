# B4RRHH — Vertical Map (FULL)

## Current Vertical Landscape

| Vertical | Type | Catalog | Temporal Rules | Operations |
|----------|------|--------|----------------|-----------|
| employee | ROOT | none | none | CRUD |
| presence | STRONG_TIMELINE base | none | NO_OVERLAP | close |
| contact | REFERENCE_CURRENT | SIMPLE | none | CRUD |
| address | REFERENCE_CURRENT | SIMPLE | none | CRUD |
| work_center | FLEXIBLE_TIMELINE | SIMPLE | NO_OVERLAP + CONTAINED | CRUD |
| cost_center | DISTRIBUTED_TIMELINE | SIMPLE | MULTI_ACTIVE + SUM<=100 | TBD |
| labor_classification | STRONG_TIMELINE | COMPOSITE | FULL_COVERAGE | replaceFromDate |
| contract | STRONG_TIMELINE | COMPOSITE | FULL_COVERAGE | replaceFromDate |

---

## Future Candidates

| Vertical | Expected Type | Notes |
|----------|--------------|------|
| assignment | STRONG_TIMELINE | complex |
| payroll_type | REFERENCE_CURRENT | simple |
| professional_group | FLEXIBLE_TIMELINE | maybe historized |
| location | FLEXIBLE_TIMELINE | linked to org |

---

## Pattern Clusters

### Strong Timeline Cluster
- presence
- labor_classification
- contract

### Distributed Cluster
- cost_center

### Reference Cluster
- contact
- address

---

## Design Strategy

1. Classify vertical FIRST
2. Define identity
3. Define temporal rules
4. Define catalog model
5. Define operations

---

## Warning Signals

If you see:
- duplicated replace logic
- duplicated coverage logic
- inconsistent identity

→ update language BEFORE coding more

---

## Evolution Path

Phase 1:
- language definition (current)

Phase 2:
- light helpers (temporal utils)

Phase 3:
- pattern consolidation

Phase 4:
- optional abstraction (if justified)

---

## Final Principle

Do not design verticals in isolation.

Always place them in:
- a type
- a pattern
- a system map
