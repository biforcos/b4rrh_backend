# ADR --- Rule System as Employee Regulatory Context Root

## Status

Proposed

## Context

In the B4RRHH domain model, the functional identity of an employee is
defined as:

    ruleSystemCode + employeeTypeCode + employeeNumber

Example:

    XXX-EMP-00001

This means that the employee belongs to a **rule system context**
(`XXX`), which determines the set of functional rules that apply to that
employee.

Originally, `rule_system` values resembled country codes (e.g., ESP,
PRT), which created a misleading semantic association between:

    rule_system ≠ country

In reality, a **rule system represents the regulatory or functional
context governing the employee**, not necessarily a geographical entity.

As the system grows, different categories of rule-driven catalogs are
appearing:

Examples:

Common catalogs - COUNTRY - ADDRESS_TYPE

Labour / HR catalogs - COMPANY - WORK_CENTER

Payroll catalogs - CONTRIBUTION_GROUP - PAYROLL_AREA

Currently, `rule_entity_type` has no attribute indicating the
**functional domain** to which the catalog belongs.

This limits the expressiveness of the metamodel and makes it harder to
reason about rule ownership and scope.

------------------------------------------------------------------------

## Problem

The current metamodel structure is:

    rule_system
    rule_entity_type
    rule_entity

`rule_entity_type` is flat and does not convey the functional scope of
the catalog it represents.

As the system evolves, catalogs naturally belong to different
**functional domains**, such as:

-   COMMON (cross-domain)
-   LABORAL / HR
-   PAYROLL

Without modeling this explicitly, the catalog layer risks becoming an
unstructured set of rule types.

------------------------------------------------------------------------

## Proposed Approach

Introduce a **functional domain classification** for `rule_entity_type`.

Add a new attribute:

    rule_entity_type.domain_code

Example values:

    COMMON
    LABORAL
    PAYROLL

Each `rule_entity_type` belongs to exactly one domain.

Example:

  rule_entity_type     domain_code
  -------------------- -------------
  COUNTRY              COMMON
  ADDRESS_TYPE         COMMON
  COMPANY              LABORAL
  WORK_CENTER          LABORAL
  CONTRIBUTION_GROUP   PAYROLL

This introduces semantic structure without introducing full hierarchical
complexity.

------------------------------------------------------------------------

## Rationale

This approach provides several benefits:

### 1. Improves semantic clarity

Catalog types become grouped by functional domain rather than remaining
an undifferentiated list.

### 2. Maintains backward compatibility

The existing schema and relationships remain intact.

No changes are required for:

    rule_entity
    employee identity

### 3. Enables future evolution

The domain classification could later evolve into a dedicated structure
such as:

    rule_domain

Or support hierarchical rule resolution strategies.

### 4. Avoids premature hierarchy implementation

This ADR deliberately **does not introduce a hierarchical rule system
model yet**.

The domain classification is a lightweight step toward a richer model.

------------------------------------------------------------------------

## Non‑Goals

This ADR does **not** introduce:

-   rule system hierarchies
-   rule inheritance
-   catalog override resolution
-   domain-specific query logic

These topics remain open for future design exploration.

------------------------------------------------------------------------

## Future Evolution (Potential)

Possible future enhancements may include:

-   Introducing a `rule_domain` table
-   Defining hierarchical rule contexts
-   Supporting rule inheritance across domains
-   Separating global vs context-specific catalogs

Example conceptual model:

    rule_system (context root)
        ├── COMMON
        ├── LABORAL
        └── PAYROLL

Where catalogs are attached to domains rather than directly to rule
systems.

------------------------------------------------------------------------

## Consequences

### Positive

-   Adds clarity to catalog semantics
-   Keeps the current metamodel stable
-   Enables better reasoning about rule ownership

### Negative

-   Adds a new attribute to `rule_entity_type`
-   Requires classification decisions when adding new catalog types

------------------------------------------------------------------------

## Open Questions

1.  Should `domain_code` remain an enum-like field or evolve into a
    table (`rule_domain`)?

2.  Should rule domains eventually support inheritance between rule
    systems?

3.  Should some domains be global (shared across rule systems)?

These questions are intentionally deferred until the rule model matures
further.
