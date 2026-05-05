# Payroll Type Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restrict `payrollTypeCode` to `NORMAL` and `EXTRA` everywhere — DB CHECK constraints, domain validation, updated test data, OpenAPI enum, and frontend dropdown.

**Architecture:** `payrollTypeCode` stays as `String` throughout (no enum type change). Enforcement points: (1) domain `Payroll` private constructor validates via `PayrollTypeCodes.isValid()`, (2) DB CHECK constraint on 4 tables. Frontend uses a native `<select>` instead of free-text input.

**Tech Stack:** Java 21, Spring Boot, Flyway, Angular 21, PrimeNG, PowerShell (Windows)

**Repos:**
- Backend: `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_backend`
- Frontend: `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_frontend`

---

## File Map

| Action | File |
|--------|------|
| Create | `src/main/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodes.java` |
| Create | `src/main/java/com/b4rrhh/payroll/domain/exception/PayrollTypeInvalidException.java` |
| Create | `src/test/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodesTest.java` |
| Modify | `src/main/java/com/b4rrhh/payroll/domain/model/Payroll.java` (private constructor) |
| Modify | `src/test/java/com/b4rrhh/payroll/domain/model/PayrollTest.java` (add 1 test) |
| Modify | `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java` |
| Create | `src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandlerTest.java` |
| Bulk modify | All `*.java` in `src/test/java/com/b4rrhh/payroll/` — replace `"ORD"` and `"MENSUAL"` payrollTypeCode values |
| Create | `src/main/resources/db/migration/V94__add_payroll_type_code_constraint.sql` |
| Modify | `openapi/payroll-api.yaml` — add `enum: [NORMAL, EXTRA]` to payrollTypeCode |
| Modify | `openapi/personnel-administration-api.yaml` — same |
| Modify | `src/app/features/nomina/operaciones/store/operaciones.store.ts` |
| Modify | `src/app/features/nomina/operaciones/ui/operaciones-page.component.ts` |
| Modify | `src/app/features/nomina/operaciones/ui/operaciones-page.component.html` |

---

## Task 1: PayrollTypeCodes + PayrollTypeInvalidException

**Files:**
- Create: `src/main/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodes.java`
- Create: `src/main/java/com/b4rrhh/payroll/domain/exception/PayrollTypeInvalidException.java`
- Create (test): `src/test/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodesTest.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodesTest.java`:

```java
package com.b4rrhh.payroll.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollTypeCodesTest {

    @Test
    void normalIsValid() {
        assertTrue(PayrollTypeCodes.isValid("NORMAL"));
    }

    @Test
    void extraIsValid() {
        assertTrue(PayrollTypeCodes.isValid("EXTRA"));
    }

    @Test
    void ordIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid("ORD"));
    }

    @Test
    void mensualIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid("MENSUAL"));
    }

    @Test
    void nullIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid(null));
    }

    @Test
    void emptyIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid(""));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```
mvn test -Dtest=PayrollTypeCodesTest -pl . 2>&1 | tail -20
```

Expected: FAIL — compilation error (`PayrollTypeCodes` doesn't exist yet).

- [ ] **Step 3: Create PayrollTypeCodes**

`src/main/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodes.java`:

```java
package com.b4rrhh.payroll.domain.model;

import java.util.Set;

public final class PayrollTypeCodes {

    public static final String NORMAL = "NORMAL";
    public static final String EXTRA  = "EXTRA";

    private static final Set<String> VALID = Set.of(NORMAL, EXTRA);

    private PayrollTypeCodes() {}

    public static boolean isValid(String code) {
        return code != null && VALID.contains(code);
    }
}
```

- [ ] **Step 4: Create PayrollTypeInvalidException**

`src/main/java/com/b4rrhh/payroll/domain/exception/PayrollTypeInvalidException.java`:

```java
package com.b4rrhh.payroll.domain.exception;

public class PayrollTypeInvalidException extends RuntimeException {

    public PayrollTypeInvalidException(String code) {
        super("Invalid payrollTypeCode: '" + code + "'");
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```
mvn test -Dtest=PayrollTypeCodesTest -pl . 2>&1 | tail -10
```

Expected: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```
git add src/main/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodes.java
git add src/main/java/com/b4rrhh/payroll/domain/exception/PayrollTypeInvalidException.java
git add src/test/java/com/b4rrhh/payroll/domain/model/PayrollTypeCodesTest.java
git commit -m "feat(payroll): add PayrollTypeCodes constants and PayrollTypeInvalidException"
```

---

## Task 2: Payroll Constructor Validation

**Files:**
- Modify: `src/main/java/com/b4rrhh/payroll/domain/model/Payroll.java`
- Modify: `src/test/java/com/b4rrhh/payroll/domain/model/PayrollTest.java`

- [ ] **Step 1: Add the failing test to PayrollTest.java**

Add this test to the existing `PayrollTest` class:

```java
@Test
void rejectsInvalidPayrollTypeCode() {
    assertThrows(
            PayrollTypeInvalidException.class,
            () -> Payroll.create(
                    "ESP",
                    "INTERNAL",
                    "0001",
                    "202501",
                    "ORD",   // invalid — must be NORMAL or EXTRA
                    1,
                    PayrollStatus.CALCULATED,
                    null,
                    LocalDateTime.of(2026, 1, 31, 10, 15),
                    "PAYROLL_ENGINE",
                    "1.0.0",
                    List.of(
                            new PayrollConcept(
                                    1,
                                    "BASE",
                                    "Base salary",
                                    new BigDecimal("1000.00"),
                                    new BigDecimal("1.00"),
                                    new BigDecimal("1000.00"),
                                    "EARNING",
                                    "202501",
                                    1
                            )
                    ),
                    List.of(
                            new PayrollContextSnapshot(
                                    "PRESENCE",
                                    "EMPLOYEE",
                                    "{\"presenceNumber\":1}",
                                    "{\"companyCode\":\"ES01\"}"
                            )
                    )
            )
    );
}
```

Add the import at the top of `PayrollTest.java`:
```java
import com.b4rrhh.payroll.domain.exception.PayrollTypeInvalidException;
```

Also update the existing `payroll()` helper to use a valid code so it doesn't break other tests:
- Find: `"ORD",` (the payrollTypeCode argument in the `payroll()` helper)
- Replace with: `"NORMAL",`

- [ ] **Step 2: Run test to verify it fails**

```
mvn test -Dtest=PayrollTest -pl . 2>&1 | tail -15
```

Expected: `rejectsInvalidPayrollTypeCode` FAILS — no validation thrown yet. Other tests may also fail because `"ORD"` is still accepted.

- [ ] **Step 3: Add validation to Payroll.java private constructor**

In `src/main/java/com/b4rrhh/payroll/domain/model/Payroll.java`:

Add import:
```java
import com.b4rrhh.payroll.domain.exception.PayrollTypeInvalidException;
```

In the private constructor, find this line:
```java
        this.payrollTypeCode = requireCode(payrollTypeCode, "payrollTypeCode", 30);
```

Replace with:
```java
        String normalizedPayrollTypeCode = requireCode(payrollTypeCode, "payrollTypeCode", 30);
        if (!PayrollTypeCodes.isValid(normalizedPayrollTypeCode)) {
            throw new PayrollTypeInvalidException(normalizedPayrollTypeCode);
        }
        this.payrollTypeCode = normalizedPayrollTypeCode;
```

- [ ] **Step 4: Run test to verify it passes**

```
mvn test -Dtest=PayrollTest -pl . 2>&1 | tail -10
```

Expected: `Tests run: 7, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```
git add src/main/java/com/b4rrhh/payroll/domain/model/Payroll.java
git add src/test/java/com/b4rrhh/payroll/domain/model/PayrollTest.java
git commit -m "feat(payroll): validate payrollTypeCode in Payroll constructor"
```

---

## Task 3: PayrollExceptionHandler Mapping

**Files:**
- Create (test): `src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandlerTest.java`
- Modify: `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java`

- [ ] **Step 1: Write the failing test**

`src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandlerTest.java`:

```java
package com.b4rrhh.payroll.infrastructure.web;

import com.b4rrhh.payroll.domain.exception.PayrollTypeInvalidException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollExceptionHandlerTest {

    private final PayrollExceptionHandler handler = new PayrollExceptionHandler();

    @Test
    void mapsPayrollTypeInvalidExceptionToBadRequest() {
        var ex = new PayrollTypeInvalidException("ORD");

        var response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid payrollTypeCode: 'ORD'", response.getBody().message());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```
mvn test -Dtest=PayrollExceptionHandlerTest -pl . 2>&1 | tail -15
```

Expected: FAIL — `PayrollTypeInvalidException` is not handled by `handleBadRequest` (Spring exception handler won't catch it at the method level in unit test; the response will either throw or not be registered).

- [ ] **Step 3: Add PayrollTypeInvalidException to the handler**

In `src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java`:

Add import:
```java
import com.b4rrhh.payroll.domain.exception.PayrollTypeInvalidException;
```

Change:
```java
    @ExceptionHandler({
            InvalidPayrollArgumentException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<PayrollErrorResponse> handleBadRequest(RuntimeException ex) {
```

To:
```java
    @ExceptionHandler({
            InvalidPayrollArgumentException.class,
            IllegalArgumentException.class,
            PayrollTypeInvalidException.class
    })
    public ResponseEntity<PayrollErrorResponse> handleBadRequest(RuntimeException ex) {
```

- [ ] **Step 4: Run test to verify it passes**

```
mvn test -Dtest=PayrollExceptionHandlerTest -pl . 2>&1 | tail -10
```

Expected: `Tests run: 1, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```
git add src/main/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandler.java
git add src/test/java/com/b4rrhh/payroll/infrastructure/web/PayrollExceptionHandlerTest.java
git commit -m "feat(payroll): map PayrollTypeInvalidException to 400 Bad Request"
```

---

## Task 4: Update Test Data (ORD/MENSUAL → NORMAL)

**Context:** After Task 2, `Payroll.create()` rejects anything not `NORMAL`/`EXTRA`. All existing payroll tests use `"ORD"` or `"MENSUAL"` as `payrollTypeCode` — they will fail. This task fixes all of them.

**Important distinction:**
- `"ORD"` in payroll tests is **always** `payrollTypeCode` → replace with `"NORMAL"`
- `"MENSUAL"` in payroll tests is **sometimes** `payrollTypeCode` (RecalculatePayrollServiceTest, SearchPayrollsServiceTest, PayrollControllerTest) → replace with `"NORMAL"` **only in those 3 files**
- `"MENSUAL"` in `LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest.java` line ~100 is `TipoNomina` for AgreementCategoryProfile → **do NOT change it**

- [ ] **Step 1: Replace "ORD" → "NORMAL" in all payroll test files**

Run from `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_backend`:

```powershell
Get-ChildItem -Path "src\test\java\com\b4rrhh\payroll" -Filter "*.java" -Recurse |
  ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    $updated = $content -replace '"ORD"', '"NORMAL"'
    if ($content -ne $updated) {
      Set-Content $_.FullName $updated -NoNewline
      Write-Host "Updated: $($_.Name)"
    }
  }
```

Expected output: lists ~16 files updated.

- [ ] **Step 2: Replace "MENSUAL" → "NORMAL" as payrollTypeCode in the 3 specific files**

```powershell
$files = @(
    "src\test\java\com\b4rrhh\payroll\application\usecase\RecalculatePayrollServiceTest.java",
    "src\test\java\com\b4rrhh\payroll\application\usecase\SearchPayrollsServiceTest.java",
    "src\test\java\com\b4rrhh\payroll\infrastructure\web\PayrollControllerTest.java"
)
foreach ($file in $files) {
    $content = Get-Content $file -Raw
    $updated = $content -replace '"MENSUAL"', '"NORMAL"'
    if ($content -ne $updated) {
        Set-Content $file $updated -NoNewline
        Write-Host "Updated: $file"
    }
}
```

- [ ] **Step 3: Verify LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest.java is untouched**

Open the file and confirm that line ~100 still has `"MENSUAL"` (for TipoNomina), while lines ~109 and ~130 now show `"NORMAL"` (for payrollTypeCode).

```powershell
Select-String -Path "src\test\java\com\b4rrhh\payroll\application\usecase\LaunchPayrollCalculationEligibleRealEndToEndIntegrationTest.java" -Pattern "MENSUAL|NORMAL"
```

Expected: one line with `MENSUAL` (the tipoNomina argument) and two lines with `NORMAL` (the payrollTypeCode arguments).

- [ ] **Step 4: Run the payroll test suite**

```
mvn test -Dtest="com.b4rrhh.payroll.**" -pl . 2>&1 | tail -20
```

Expected: all payroll tests pass. If any test fails with `PayrollTypeInvalidException`, check the file — it's using an invalid payrollTypeCode value that was missed.

- [ ] **Step 5: Commit**

```
git add src/test/java/com/b4rrhh/payroll/
git commit -m "test(payroll): replace ORD/MENSUAL with NORMAL in all payroll test fixtures"
```

---

## Task 5: V94 DB Migration — CHECK Constraints

**Files:**
- Create: `src/main/resources/db/migration/V94__add_payroll_type_code_constraint.sql`

- [ ] **Step 1: Create the migration file**

`src/main/resources/db/migration/V94__add_payroll_type_code_constraint.sql`:

```sql
-- =========================================================
-- V94__add_payroll_type_code_constraint.sql
-- Restrict payroll_type_code to NORMAL and EXTRA in all payroll tables.
-- First normalize any legacy values (ORD, MENSUAL) to NORMAL,
-- then add CHECK constraints.
-- =========================================================

-- 1. Normalize legacy values

update payroll.payroll
set payroll_type_code = 'NORMAL'
where payroll_type_code not in ('NORMAL', 'EXTRA');

update payroll.calculation_run
set payroll_type_code = 'NORMAL'
where payroll_type_code not in ('NORMAL', 'EXTRA');

update payroll.calculation_claim
set payroll_type_code = 'NORMAL'
where payroll_type_code not in ('NORMAL', 'EXTRA');

update payroll.calculation_run_message
set payroll_type_code = 'NORMAL'
where payroll_type_code is not null
  and payroll_type_code not in ('NORMAL', 'EXTRA');

-- 2. Add CHECK constraints

alter table payroll.payroll
    add constraint chk_payroll_type_code
    check (payroll_type_code in ('NORMAL', 'EXTRA'));

alter table payroll.calculation_run
    add constraint chk_calculation_run_payroll_type_code
    check (payroll_type_code in ('NORMAL', 'EXTRA'));

alter table payroll.calculation_claim
    add constraint chk_calculation_claim_payroll_type_code
    check (payroll_type_code in ('NORMAL', 'EXTRA'));

alter table payroll.calculation_run_message
    add constraint chk_calculation_run_message_payroll_type_code
    check (payroll_type_code is null or payroll_type_code in ('NORMAL', 'EXTRA'));
```

Note: `calculation_run_message.payroll_type_code` is nullable (VARCHAR(30), no NOT NULL in V55), so the constraint uses `IS NULL OR`.

- [ ] **Step 2: Run the full test suite**

```
mvn test 2>&1 | tail -20
```

Expected: the migration runs on H2 in-memory and all tests pass. If Flyway fails to apply V94, check the SQL syntax — H2 may need slight adjustments (H2 supports standard ALTER TABLE ... ADD CONSTRAINT CHECK syntax).

- [ ] **Step 3: Commit**

```
git add src/main/resources/db/migration/V94__add_payroll_type_code_constraint.sql
git commit -m "feat(payroll): V94 — add CHECK constraint payroll_type_code in (NORMAL, EXTRA)"
```

---

## Task 6: OpenAPI Enum + Frontend API Refresh

**Context:** `payrollTypeCode` currently has `type: string, maxLength: 30` with no enum. Adding `enum: [NORMAL, EXTRA]` propagates through `npm run api:refresh` to generate TypeScript union types in the frontend.

**Files (backend):**
- Modify: `openapi/payroll-api.yaml`
- Modify: `openapi/personnel-administration-api.yaml`

**Files (frontend, auto-generated after refresh):**
- `src/app/core/api/generated/` (entire directory regenerated — do not edit manually)

- [ ] **Step 1: Update payroll-api.yaml — reusable PayrollTypeCode parameter component**

Find the `PayrollTypeCode` parameter component (around line 344). It looks like:
```yaml
    PayrollTypeCode:
      in: path
      name: payrollTypeCode
      required: true
      schema:
        type: string
        maxLength: 30
```

Change to:
```yaml
    PayrollTypeCode:
      in: path
      name: payrollTypeCode
      required: true
      schema:
        type: string
        maxLength: 30
        enum: [NORMAL, EXTRA]
```

- [ ] **Step 2: Update payroll-api.yaml — all inline payrollTypeCode body schema properties**

Use PowerShell to add `enum` to all inline occurrences. Run from `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_backend`:

```powershell
$file = "openapi\payroll-api.yaml"
$content = Get-Content $file -Raw
# Match "payrollTypeCode:" followed by type/maxLength lines (various indentations)
# and add enum after maxLength: 30
$updated = $content -replace '(payrollTypeCode:\r?\n(\s+)type: string\r?\n\2maxLength: 30)(?!\r?\n\2enum)', '$1' + "`n" + '${2}enum: [NORMAL, EXTRA]'
Set-Content $file $updated -NoNewline
```

After running, verify the changes look correct:
```powershell
Select-String -Path "openapi\payroll-api.yaml" -Pattern "enum: \[NORMAL" | Measure-Object | Select-Object -ExpandProperty Count
```

Expected: the count equals the number of `payrollTypeCode` schema definitions in the file (path parameter component + all body properties).

- [ ] **Step 3: Update personnel-administration-api.yaml — all payrollTypeCode properties**

Run the same PowerShell script targeting `personnel-administration-api.yaml`:

```powershell
$file = "openapi\personnel-administration-api.yaml"
$content = Get-Content $file -Raw
$updated = $content -replace '(payrollTypeCode:\r?\n(\s+)type: string\r?\n\2maxLength: 30)(?!\r?\n\2enum)', '$1' + "`n" + '${2}enum: [NORMAL, EXTRA]'
Set-Content $file $updated -NoNewline
```

For path parameters in `personnel-administration-api.yaml` (they are defined inline, not as reusable components), also add enum to the path parameter schemas. Search for `name: payrollTypeCode` sections and add `enum: [NORMAL, EXTRA]` to their schema.

- [ ] **Step 4: Verify backend tests still pass**

```
mvn test 2>&1 | tail -10
```

The OpenAPI YAML changes don't affect backend tests, but this confirms no regression.

- [ ] **Step 5: Regenerate frontend API client**

In `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_frontend`:

```
npm run api:refresh
```

Expected: `src/app/core/api/generated/` is regenerated. Check that `payrollTypeCode` in generated models now uses a union type or enum instead of plain `string`.

```powershell
Select-String -Path "src\app\core\api\generated\" -Pattern "payrollTypeCode" -Recurse | Select-Object -First 5
```

- [ ] **Step 6: Run frontend build to catch TypeScript errors**

```
npm run build 2>&1 | tail -20
```

If TypeScript errors appear because the store's `signal<string>` is now incompatible with a generated `'NORMAL' | 'EXTRA'` type — those will be fixed in Task 7. Note the error locations and proceed.

- [ ] **Step 7: Commit backend**

In `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_backend`:
```
git add openapi/payroll-api.yaml openapi/personnel-administration-api.yaml
git commit -m "feat(payroll): add enum [NORMAL, EXTRA] to payrollTypeCode in OpenAPI specs"
```

- [ ] **Step 8: Commit frontend (generated files)**

In `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_frontend`:
```
git add src/app/core/api/generated/
git commit -m "chore(api): regenerate client after payrollTypeCode enum constraint"
```

---

## Task 7: Frontend — operaciones Page Dropdown

**Context:** The operaciones page has a free-text `<input>` for "Tipo de nómina" with default `'MENSUAL'`. Replace with a `<select>` dropdown and fix the default.

**Files:**
- Modify: `src/app/features/nomina/operaciones/store/operaciones.store.ts`
- Modify: `src/app/features/nomina/operaciones/ui/operaciones-page.component.ts`
- Modify: `src/app/features/nomina/operaciones/ui/operaciones-page.component.html`

- [ ] **Step 1: Fix the store default**

In `src/app/features/nomina/operaciones/store/operaciones.store.ts`:

Find:
```typescript
  private readonly payrollTypeCodeState = signal<string>('MENSUAL');
```

Replace with:
```typescript
  private readonly payrollTypeCodeState = signal<string>('NORMAL');
```

- [ ] **Step 2: Add payrollTypeOptions to the component**

In `src/app/features/nomina/operaciones/ui/operaciones-page.component.ts`:

Add to the class body (alongside `targetModes`):
```typescript
  protected readonly payrollTypeOptions: ReadonlyArray<{ value: string; label: string }> = [
    { value: 'NORMAL', label: 'Normal' },
    { value: 'EXTRA', label: 'Extra' },
  ];
```

- [ ] **Step 3: Replace the input with a select in the template**

In `src/app/features/nomina/operaciones/ui/operaciones-page.component.html`:

Find:
```html
      <label class="ops-page__field">
        <span class="ops-page__field-label">Tipo de nómina</span>
        <input
          type="text"
          class="ops-page__input"
          [value]="store.payrollTypeCode()"
          (input)="store.setPayrollTypeCode($any($event.target).value)"
        />
      </label>
```

Replace with:
```html
      <label class="ops-page__field">
        <span class="ops-page__field-label">Tipo de nómina</span>
        <select
          class="ops-page__input"
          (change)="store.setPayrollTypeCode($any($event.target).value)"
        >
          @for (opt of payrollTypeOptions; track opt.value) {
            <option [value]="opt.value" [selected]="store.payrollTypeCode() === opt.value">
              {{ opt.label }}
            </option>
          }
        </select>
      </label>
```

- [ ] **Step 4: Run frontend tests**

In `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_frontend`:
```
npm run test 2>&1 | tail -20
```

Expected: same pass/fail ratio as before (14 pre-existing failures, ~354 passing). No new failures.

- [ ] **Step 5: Run build to verify no TypeScript errors**

```
npm run build 2>&1 | tail -15
```

Expected: build succeeds.

- [ ] **Step 6: Commit**

In `c:\Users\bifor\Documents\Proyectos\B4RRHH\b4rrhh_frontend`:
```
git add src/app/features/nomina/operaciones/store/operaciones.store.ts
git add src/app/features/nomina/operaciones/ui/operaciones-page.component.ts
git add src/app/features/nomina/operaciones/ui/operaciones-page.component.html
git commit -m "feat(payroll): replace payrollTypeCode free text input with NORMAL/EXTRA dropdown"
```

---

## Self-Review

**Spec coverage check:**
- ✅ DB CHECK constraint on 4 tables (Task 5)
- ✅ UPDATE existing rows before constraint (Task 5, migration)
- ✅ `PayrollTypeCodes` constants + `isValid()` (Task 1)
- ✅ `PayrollTypeInvalidException` (Task 1)
- ✅ Domain validation in `Payroll` constructor (Task 2)
- ✅ Exception mapped to 400 in handler (Task 3)
- ✅ Test data update ORD/MENSUAL → NORMAL (Task 4)
- ✅ OpenAPI enum (Task 6)
- ✅ Frontend dropdown + default fix (Task 7)
- ✅ No changes to JPA entities, mappers, or service signatures (none needed)

**Placeholder scan:** None found.

**Type consistency:**
- `PayrollTypeCodes.isValid(String)` used in Task 1 and Task 2 — consistent.
- `PayrollTypeInvalidException(String code)` created in Task 1, thrown in Task 2, caught in Task 3 — consistent.
- `"NORMAL"` used as replacement value throughout Tasks 2, 4, 7 — consistent.
