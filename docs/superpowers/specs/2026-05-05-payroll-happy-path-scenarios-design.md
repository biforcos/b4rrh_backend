# Payroll Happy Path Scenarios — Design Spec

**Date:** 2026-05-05
**Status:** Approved

---

## Goal

Add 4 integration tests that validate the payroll calculation engine against realistic business scenarios: partial-month hire, mid-month termination, two presences in the same period, and the most complex case — two presences each containing a working-time change (creating two segments per presence). These tests act as a living regression suite for the calculation engine.

---

## Context

The engine already has one end-to-end integration test:
`LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest` (355 lines, H2 in-memory, `create-drop`, Flyway off, `ELIGIBLE_REAL` mode). It covers the full-month baseline (January 2025, 30 days, €1,425.00 net €1,122.18) but embeds its concept-graph seed inline.

These 4 new tests share the same concept graph (15 concepts, same agreement, same daily rate €47.50) and the same test infrastructure pattern. Only the employee lifecycle data differs per scenario.

---

## Key domain clarifications

- **Segment** = a subdivision of a single presence period caused by a change in employee data (working-time %, labor classification, etc.) during the period.
- **Presence** = a continuous employment stint. Multiple presences in the same payroll period produce multiple independent `Payroll` records.
- **Calculation engine is production-ready**: all 8 calculation types produce real amounts; rounding is `HALF_UP` at scale 2.

---

## Architecture

### New files

```
src/test/java/com/b4rrhh/payroll/
  └── scenario/
      ├── PayrollScenarioFixtures.java         ← concept graph seed + employee helpers
      └── PayrollHappyPathIntegrationTest.java ← 4 scenario tests
```

### Refactor to existing file

`LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest` — inline seed methods delegated to `PayrollScenarioFixtures`. Behaviour unchanged; the test still passes.

### `PayrollScenarioFixtures`

A plain Java class (no Spring annotation) that receives a `JdbcTemplate` and exposes:

```java
// Seeds the full concept graph (rule system, 15 concepts, operands, feeds,
// assignments, activations, agreement category profile, table binding + rows)
void seedConceptGraph(String ruleSystemCode);

// Employee / presence helpers — each inserts one row and returns its generated id
long insertEmployee(String ruleSystemCode, String employeeTypeCode, String employeeNumber, ...);
long insertPresence(long employeeId, int presenceNumber, LocalDate startDate, LocalDate endDate, ...);
long insertLaborClassification(long employeeId, String agreementCode, String categoryCode, LocalDate from);
long insertWorkingTime(long employeeId, BigDecimal percentage, LocalDate from, LocalDate to);
```

`endDate` on `insertPresence` accepts `null` (open-ended / still active).
`to` on `insertWorkingTime` accepts `null` (effective until end of period or next change).

### `PayrollHappyPathIntegrationTest`

```java
@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:payroll_happy_path;MODE=PostgreSQL;...",
    "payroll.launch.execution.mode=ELIGIBLE_REAL"
})
@AutoConfigureMockMvc
@Transactional
class PayrollHappyPathIntegrationTest {
    @Autowired LaunchPayrollCalculationUseCase launch;
    @Autowired JdbcTemplate jdbc;
    PayrollScenarioFixtures fixtures;

    @BeforeEach void setUp() {
        fixtures = new PayrollScenarioFixtures(jdbc);
        fixtures.seedConceptGraph("ESP");
    }
    // ... 4 test methods
}
```

Each test method:
1. Inserts employee + presence(s) + working time(s) + labor classification via `fixtures.*`
2. Calls `launch.launch(command)` with `ruleSystemCode=ESP`, `payrollPeriodCode=202501`, `payrollTypeCode=NORMAL`
3. Asserts on the returned `CalculationRun` and queries `payroll.*` tables via JdbcTemplate

---

## Scenarios and expected amounts

All scenarios use:
- Rule system: `ESP`
- Period: `202501` (January 2025, 31 calendar days)
- Agreement: `99002405011982`, category `99002405-G2`
- Daily rate P02: €47.50 (from agreement table, grupo cotización II)
- SS rate: 4.70% · Unemployment: 1.55% · IRPF: 15.00%
- Rounding: `HALF_UP` at scale 2 after each `RATE_BY_QUANTITY` / `PERCENTAGE` step

**D01 formula**: `min(daysInSegment, 30)` — applied per segment.  
**SALARIO_BASE per segment**: `D01 × P02 × J01` where `J01 = workingTimePercentage / 100`.  
**Deductions**: applied to the period total of SALARIO_BASE (PERCENTAGE concepts are PERIOD-scoped).

---

### Scenario 1 — Partial month hire (`partialMonthHire`)

| | |
|---|---|
| Presence | Jan 15 → (open) |
| Segment | Jan 15–31 = **17 days** |
| D01 | 17 |
| J01 | 1.00 (100% full-time) |

| Concept | Code | Calculation | Amount |
|---------|------|-------------|--------|
| SALARIO_BASE | 101 | 17 × 47.50 × 1.00 | **€807.50** |
| CC_TRABAJADOR | 700 | 807.50 × 4.70% | **€37.95** |
| DESEMPLEO_TRABAJADOR | 703 | 807.50 × 1.55% | **€12.52** |
| RETENCION_IRPF | 800 | 807.50 × 15.00% | **€121.13** |
| TOTAL_DEVENGOS | 970 | | **€807.50** |
| TOTAL_DEDUCCIONES | 980 | 37.95 + 12.52 + 121.13 | **€171.60** |
| LIQUIDO_A_PAGAR | 990 | 807.50 − 171.60 | **€635.90** |

Structure assertions: 1 payroll · 1 segment · 7 concepts persisted · status CALCULATED.

**Rounding notes**: 807.50 × 1.55% = 12.51625 → **12.52** (HALF_UP). 807.50 × 15% = 121.125 → **121.13** (HALF_UP).

---

### Scenario 2 — Mid-month termination (`midMonthTermination`)

| | |
|---|---|
| Presence | Jan 1 → Jan 20 |
| Segment | Jan 1–20 = **20 days** |
| D01 | 20 |
| J01 | 1.00 |

| Concept | Code | Amount |
|---------|------|--------|
| SALARIO_BASE | 101 | **€950.00** |
| CC_TRABAJADOR | 700 | **€44.65** |
| DESEMPLEO_TRABAJADOR | 703 | **€14.73** |
| RETENCION_IRPF | 800 | **€142.50** |
| TOTAL_DEVENGOS | 970 | **€950.00** |
| TOTAL_DEDUCCIONES | 980 | **€201.88** |
| LIQUIDO_A_PAGAR | 990 | **€748.12** |

Structure assertions: 1 payroll · 1 segment · 7 concepts · status CALCULATED.

**Rounding notes**: 950.00 × 1.55% = 14.725 → **14.73** (HALF_UP).

---

### Scenario 3 — Two clean presences (`twoPresencesSamePeriod`)

| | Presence #1 | Presence #2 |
|---|---|---|
| Dates | Jan 1 → Jan 15 | Jan 16 → Jan 30 |
| Days | 15 | 15 |
| D01 | 15 | 15 |
| J01 | 1.00 | 1.00 |

Both payrolls produce identical amounts:

| Concept | Code | Amount |
|---------|------|--------|
| SALARIO_BASE | 101 | **€712.50** |
| CC_TRABAJADOR | 700 | **€33.49** |
| DESEMPLEO_TRABAJADOR | 703 | **€11.04** |
| RETENCION_IRPF | 800 | **€106.88** |
| TOTAL_DEVENGOS | 970 | **€712.50** |
| TOTAL_DEDUCCIONES | 980 | **€151.41** |
| LIQUIDO_A_PAGAR | 990 | **€561.09** |

Structure assertions: **2 payrolls** · 1 segment each · 7 concepts each · both CALCULATED.  
Key assertion: the launch command targets the employee and produces exactly 2 payrolls for period 202501.

**Rounding notes**: 712.50 × 4.70% = 33.4875 → **33.49**. 712.50 × 1.55% = 11.04375 → **11.04**. 712.50 × 15% = 106.875 → **106.88**.

---

### Scenario 4 — Two presences with working-time change (`twoPresencesWithSegments`)

The most complex case. Each presence has a working-time change mid-presence, creating 2 segments per payroll.

**Presence #1: Jan 1–15**

| Segment | Dates | Days | J01 | D01 × P02 × J01 |
|---------|-------|------|-----|-----------------|
| 1a | Jan 1–9 | 9 | 1.00 | 9 × 47.50 = €427.50 |
| 1b | Jan 10–15 | 6 | 0.50 | 6 × 47.50 × 0.50 = €142.50 |

Working time: two `insertWorkingTime` calls — 100% from Jan 1 (to Jan 9), 50% from Jan 10 (to null / end of period).

| Concept | Code | Amount |
|---------|------|--------|
| SALARIO_BASE | 101 | **€570.00** |
| CC_TRABAJADOR | 700 | **€26.79** |
| DESEMPLEO_TRABAJADOR | 703 | **€8.84** |
| RETENCION_IRPF | 800 | **€85.50** |
| TOTAL_DEVENGOS | 970 | **€570.00** |
| TOTAL_DEDUCCIONES | 980 | **€121.13** |
| LIQUIDO_A_PAGAR | 990 | **€448.87** |

**Rounding notes**: 570.00 × 1.55% = 8.835 → **8.84** (HALF_UP). Deductions: 26.79 + 8.84 + 85.50 = **121.13**.

---

**Presence #2: Jan 16–30**

| Segment | Dates | Days | J01 | D01 × P02 × J01 |
|---------|-------|------|-----|-----------------|
| 2a | Jan 16–25 | 10 | 1.00 | 10 × 47.50 = €475.00 |
| 2b | Jan 26–30 | 5 | 0.50 | 5 × 47.50 × 0.50 = €118.75 |

Working time: two `insertWorkingTime` calls — 100% from Jan 16 (to Jan 25), 50% from Jan 26 (to null / end of period).

| Concept | Code | Amount |
|---------|------|--------|
| SALARIO_BASE | 101 | **€593.75** |
| CC_TRABAJADOR | 700 | **€27.91** |
| DESEMPLEO_TRABAJADOR | 703 | **€9.20** |
| RETENCION_IRPF | 800 | **€89.06** |
| TOTAL_DEVENGOS | 970 | **€593.75** |
| TOTAL_DEDUCCIONES | 980 | **€126.17** |
| LIQUIDO_A_PAGAR | 990 | **€467.58** |

**Rounding notes**: 593.75 × 4.70% = 27.90625 → **27.91**. 593.75 × 1.55% = 9.203125 → **9.20**. 593.75 × 15% = 89.0625 → **89.06**.

Structure assertions: **2 payrolls** · **2 segments each** · 7 concepts each · both CALCULATED.

---

## Assert strategy

Each test asserts in three layers:

1. **Run-level**: `CalculationRun.status = COMPLETED`, `totalCalculated = N` (1 or 2)
2. **Payroll-level**: status = CALCULATED, correct segment count, correct concept count (7)
3. **Amount-level**: per-concept `BigDecimal` comparison using `compareTo == 0` (no scale sensitivity)

If the engine produces a different amount from what the spec states, the test fails — the fix is in the engine or the manual calculation, never in the assert.

---

## What is NOT covered

- IRPF percentage changes between segments (not in the current concept graph)
- Multiple agreement category changes within a presence (segment type not yet exercised)
- `EXTRA` payroll type (separate concern)
- Recalculation / invalidation lifecycle

---

## Migration / database impact

None. All tests use H2 in-memory with `ddl-auto=create-drop`. No new Flyway migrations.
