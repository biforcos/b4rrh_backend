# Agreement Category Cotización Profile Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Store SS cotización metadata in the rule system — `grupo_cotizacion_code` + `tipo_nomina` as an enriched profile on each `AGREEMENT_CATEGORY`, and `epigrafe_at_code` on each `COMPANY` profile — so the payroll engine can later consume this data without any manual input from HR users.

**Architecture:** New `rulesystem.agreementcategoryprofile` vertical (mirrors `companyprofile` exactly) upserts SS data per AGREEMENT_CATEGORY rule entity. Company profile gains a nullable `epigrafe_at_code` column. No new employee vertical is needed — employees inherit cotización data through their existing `labor_classification → agreement_category` link. The `GRUPO_COTIZACION` catalog (11 standard Spanish SS groups) is seeded into `rule_entity` and validated on every upsert.

**Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL/H2, Flyway, JUnit 5 + Mockito, MockMvc (standaloneSetup)

---

## File Map

### New Flyway migrations
| File | Purpose |
|------|---------|
| `src/main/resources/db/migration/V84__seed_grupo_cotizacion_catalog.sql` | `GRUPO_COTIZACION` rule_entity_type + 11 entries |
| `src/main/resources/db/migration/V85__create_agreement_category_profile_table.sql` | `rulesystem.agreement_category_profile` table |
| `src/main/resources/db/migration/V86__add_epigrafe_at_code_to_company_profile.sql` | Nullable `epigrafe_at_code` column on `rulesystem.company_profile` |
| `src/main/resources/db/migration/V87__seed_agreement_category_profile.sql` | Links ESP seed categories to their grupos |

### Modified — Company Profile vertical
| File | Change |
|------|--------|
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/domain/model/CompanyProfile.java` | Add `epigrafeAtCode` field, constructor param, `update()` param, getter |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/persistence/CompanyProfileEntity.java` | Add `epigrafe_at_code` column mapping |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/application/usecase/UpsertCompanyProfileCommand.java` | Add `epigrafeAtCode` |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/application/usecase/UpsertCompanyProfileService.java` | Thread `epigrafeAtCode` through create and `update()` |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/dto/UpsertCompanyProfileRequest.java` | Add `epigrafeAtCode` |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/dto/CompanyProfileResponse.java` | Add `epigrafeAtCode` |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/assembler/CompanyProfileResponseAssembler.java` | Map `epigrafeAtCode` |
| `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/persistence/CompanyProfilePersistenceAdapter.java` | Map `epigrafeAtCode` in `save()` and `toDomain()` |
| `src/test/java/com/b4rrhh/rulesystem/companyprofile/application/usecase/UpsertCompanyProfileServiceTest.java` | Add epigrafe test case |
| `src/test/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/CompanyProfileControllerHttpTest.java` | Add epigrafe assertion |

### New — AgreementCategoryProfile vertical (`com.b4rrhh.rulesystem.agreementcategoryprofile`)
| File | Purpose |
|------|---------|
| `domain/model/AgreementCategoryProfile.java` | Immutable domain value object |
| `domain/model/TipoNomina.java` | Enum: `MENSUAL`, `DIARIO` |
| `domain/port/AgreementCategoryProfileRepository.java` | Find + save port |
| `domain/exception/AgreementCategoryProfileNotFoundException.java` | GET with no profile |
| `domain/exception/AgreementCategoryProfileCategoryNotFoundException.java` | Category code unknown |
| `domain/exception/GrupoCotizacionInvalidException.java` | Grupo code not in catalog |
| `application/usecase/GetAgreementCategoryProfileQuery.java` | Query record |
| `application/usecase/GetAgreementCategoryProfileUseCase.java` | Get interface |
| `application/usecase/GetAgreementCategoryProfileService.java` | Get implementation |
| `application/usecase/UpsertAgreementCategoryProfileCommand.java` | Command record |
| `application/usecase/UpsertAgreementCategoryProfileUseCase.java` | Upsert interface |
| `application/usecase/UpsertAgreementCategoryProfileService.java` | Upsert implementation |
| `infrastructure/persistence/AgreementCategoryProfileEntity.java` | JPA entity |
| `infrastructure/persistence/SpringDataAgreementCategoryProfileRepository.java` | Spring Data repo |
| `infrastructure/persistence/AgreementCategoryProfilePersistenceAdapter.java` | Implements domain port |
| `infrastructure/web/dto/UpsertAgreementCategoryProfileRequest.java` | Request DTO |
| `infrastructure/web/dto/AgreementCategoryProfileResponse.java` | Response DTO |
| `infrastructure/web/dto/AgreementCategoryProfileErrorResponse.java` | Error DTO |
| `infrastructure/web/assembler/AgreementCategoryProfileResponseAssembler.java` | Domain → DTO |
| `infrastructure/web/AgreementCategoryProfileController.java` | GET + PUT `/agreement-categories/{ruleSystemCode}/{categoryCode}/profile` |
| `infrastructure/web/AgreementCategoryProfileExceptionHandler.java` | Exception → HTTP |
| `src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/GetAgreementCategoryProfileServiceTest.java` | Unit tests for GET service |
| `src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/UpsertAgreementCategoryProfileServiceTest.java` | Unit tests for UPSERT service |
| `src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/AgreementCategoryProfileControllerHttpTest.java` | Controller unit tests (standaloneSetup) |

### Modified — OpenAPI spec
| File | Change |
|------|--------|
| `openapi/personnel-administration-api.yaml` | New endpoints + schemas for agreement category profile; `epigrafeAtCode` on company profile schemas |

---

## Tasks

### Task 1: V84 — Seed GRUPO_COTIZACION catalog

**Files:**
- Create: `src/main/resources/db/migration/V84__seed_grupo_cotizacion_catalog.sql`

- [ ] **Step 1: Create the migration**

```sql
-- =========================================================
-- V84__seed_grupo_cotizacion_catalog.sql
-- Seed GRUPO_COTIZACION rule_entity_type and 11 standard ESP entries
-- =========================================================

insert into rulesystem.rule_entity_type (code, name, description)
values ('GRUPO_COTIZACION', 'Grupo de Cotización SS', 'Grupo de cotización a la Seguridad Social (1-11)')
on conflict (code) do update
    set name = excluded.name,
        description = excluded.description;

insert into rulesystem.rule_entity (rule_system_code, rule_entity_type_code, code, name, description, active, start_date)
select
    rs.code,
    'GRUPO_COTIZACION',
    g.code,
    g.name,
    g.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('01', 'Ingenieros y Licenciados', 'Personal de alta dirección no incluido en el art. 1.3.c) ET. Cotización mensual.'),
        ('02', 'Ingenieros Técnicos, Peritos y Ayudantes Titulados', 'Cotización mensual.'),
        ('03', 'Jefes Administrativos y de Taller', 'Cotización mensual.'),
        ('04', 'Ayudantes no Titulados', 'Cotización mensual.'),
        ('05', 'Oficiales Administrativos', 'Cotización mensual.'),
        ('06', 'Subalternos', 'Cotización mensual.'),
        ('07', 'Auxiliares Administrativos', 'Cotización mensual.'),
        ('08', 'Oficiales de primera y segunda', 'Cotización diaria.'),
        ('09', 'Oficiales de tercera y Especialistas', 'Cotización diaria.'),
        ('10', 'Peones', 'Cotización diaria.'),
        ('11', 'Trabajadores menores de dieciocho años, cualquier categoría', 'Cotización diaria.')
) as g(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
    set name = excluded.name,
        description = excluded.description,
        active = excluded.active;
```

- [ ] **Step 2: Run tests to verify migration applies cleanly**

```bash
cd b4rrhh_backend && mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V84__seed_grupo_cotizacion_catalog.sql
git commit -m "feat: seed GRUPO_COTIZACION SS catalog (groups 01-11)"
```

---

### Task 2: V85 — Create agreement_category_profile table

**Files:**
- Create: `src/main/resources/db/migration/V85__create_agreement_category_profile_table.sql`

- [ ] **Step 1: Create the migration**

```sql
-- =========================================================
-- V85__create_agreement_category_profile_table.sql
-- Create rulesystem.agreement_category_profile
-- =========================================================

create table rulesystem.agreement_category_profile (
    id                                    bigint generated always as identity primary key,
    agreement_category_rule_entity_id     bigint      not null,
    grupo_cotizacion_code                 varchar(2)  not null,
    tipo_nomina                           varchar(10) not null,
    created_at                            timestamp   not null default now(),
    updated_at                            timestamp   not null default now(),
    constraint uk_agreement_category_profile
        unique (agreement_category_rule_entity_id),
    constraint chk_tipo_nomina
        check (tipo_nomina in ('MENSUAL', 'DIARIO')),
    constraint fk_acp_category
        foreign key (agreement_category_rule_entity_id)
        references rulesystem.rule_entity(id)
);
```

- [ ] **Step 2: Run tests**

```bash
mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V85__create_agreement_category_profile_table.sql
git commit -m "feat: create agreement_category_profile table"
```

---

### Task 3: V86 + V87 — Epigrafe column + seed profiles

**Files:**
- Create: `src/main/resources/db/migration/V86__add_epigrafe_at_code_to_company_profile.sql`
- Create: `src/main/resources/db/migration/V87__seed_agreement_category_profile.sql`

- [ ] **Step 1: Create V86**

```sql
-- =========================================================
-- V86__add_epigrafe_at_code_to_company_profile.sql
-- Add nullable epigrafe AT/EP code to rulesystem.company_profile
-- =========================================================

alter table rulesystem.company_profile
    add column epigrafe_at_code varchar(10);
```

- [ ] **Step 2: Create V87**

Links the three ESP mock categories (seeded in V25) to their standard grupos.

```sql
-- =========================================================
-- V87__seed_agreement_category_profile.sql
-- Link ESP seed agreement categories to their grupo de cotización
-- =========================================================

insert into rulesystem.agreement_category_profile (
    agreement_category_rule_entity_id,
    grupo_cotizacion_code,
    tipo_nomina
)
select
    cat.id,
    mapping.grupo_cotizacion_code,
    mapping.tipo_nomina
from rulesystem.rule_entity cat
join (
    values
        ('CAT_ADMIN',   '05', 'MENSUAL'),
        ('CAT_TECH_1',  '01', 'MENSUAL'),
        ('CAT_TECH_2',  '02', 'MENSUAL')
) as mapping(category_code, grupo_cotizacion_code, tipo_nomina)
    on mapping.category_code = cat.code
where cat.rule_entity_type_code = 'AGREEMENT_CATEGORY'
on conflict (agreement_category_rule_entity_id) do update
    set grupo_cotizacion_code = excluded.grupo_cotizacion_code,
        tipo_nomina           = excluded.tipo_nomina,
        updated_at            = now();
```

- [ ] **Step 3: Run tests**

```bash
mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/migration/V86__add_epigrafe_at_code_to_company_profile.sql \
        src/main/resources/db/migration/V87__seed_agreement_category_profile.sql
git commit -m "feat: add epigrafe_at_code to company_profile; seed agreement_category_profile"
```

---

### Task 4: [TDD] Extend CompanyProfile with epigrafeAtCode

**Files:**
- Modify: `src/test/java/com/b4rrhh/rulesystem/companyprofile/application/usecase/UpsertCompanyProfileServiceTest.java`
- Modify: `src/test/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/CompanyProfileControllerHttpTest.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/domain/model/CompanyProfile.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/persistence/CompanyProfileEntity.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/application/usecase/UpsertCompanyProfileCommand.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/application/usecase/UpsertCompanyProfileService.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/persistence/CompanyProfilePersistenceAdapter.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/dto/UpsertCompanyProfileRequest.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/dto/CompanyProfileResponse.java`
- Modify: `src/main/java/com/b4rrhh/rulesystem/companyprofile/infrastructure/web/assembler/CompanyProfileResponseAssembler.java`

- [ ] **Step 1: Add failing test to UpsertCompanyProfileServiceTest**

Add this test method to the existing `UpsertCompanyProfileServiceTest` class:

```java
@Test
void storesEpigrafeAtCodeWhenProvided() {
    when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "COMPANY", "ACME", LocalDate.now()))
            .thenReturn(Optional.of(ruleEntity(10L, "ESP", "COMPANY", "ACME")));
    when(companyProfileRepository.findByCompanyRuleEntityId(10L)).thenReturn(Optional.empty());
    when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "COUNTRY", "ESP", LocalDate.now()))
            .thenReturn(Optional.of(ruleEntity(99L, "ESP", "COUNTRY", "ESP")));
    when(companyProfileRepository.save(any(Long.class), any(CompanyProfile.class)))
            .thenAnswer(invocation -> invocation.getArgument(1));

    CompanyProfile result = service.upsert(new UpsertCompanyProfileCommand(
            "ESP", "ACME", "Acme Spain SA", null,
            null, null, null, null, "ESP", "6210"
    ));

    assertEquals("6210", result.getEpigrafeAtCode());
}
```

- [ ] **Step 2: Run test — expect compile error**

```bash
mvn test -Dtest=UpsertCompanyProfileServiceTest -q 2>&1 | head -20
```
Expected: compile error — `UpsertCompanyProfileCommand` does not have `epigrafeAtCode` param, `CompanyProfile` has no `getEpigrafeAtCode()`

- [ ] **Step 3: Update CompanyProfile domain model**

Replace the full content of `CompanyProfile.java`:

```java
package com.b4rrhh.rulesystem.companyprofile.domain.model;

public class CompanyProfile {

    private static final int LEGAL_NAME_MAX_LENGTH = 200;
    private static final int TAX_IDENTIFIER_MAX_LENGTH = 50;
    private static final int STREET_MAX_LENGTH = 300;
    private static final int CITY_MAX_LENGTH = 120;
    private static final int POSTAL_CODE_MAX_LENGTH = 20;
    private static final int REGION_CODE_MAX_LENGTH = 30;
    private static final int EPIGRAFE_AT_CODE_MAX_LENGTH = 10;

    private final String legalName;
    private final String taxIdentifier;
    private final String street;
    private final String city;
    private final String postalCode;
    private final String regionCode;
    private final String countryCode;
    private final String epigrafeAtCode;

    public CompanyProfile(
            String legalName,
            String taxIdentifier,
            String street,
            String city,
            String postalCode,
            String regionCode,
            String countryCode,
            String epigrafeAtCode
    ) {
        this.legalName = normalizeRequiredText("legalName", legalName, LEGAL_NAME_MAX_LENGTH);
        this.taxIdentifier = normalizeOptionalText("taxIdentifier", taxIdentifier, TAX_IDENTIFIER_MAX_LENGTH);
        this.street = normalizeOptionalText("street", street, STREET_MAX_LENGTH);
        this.city = normalizeOptionalText("city", city, CITY_MAX_LENGTH);
        this.postalCode = normalizeOptionalText("postalCode", postalCode, POSTAL_CODE_MAX_LENGTH);
        this.regionCode = normalizeOptionalCode("regionCode", regionCode, REGION_CODE_MAX_LENGTH);
        this.countryCode = normalizeOptionalCode(countryCode);
        this.epigrafeAtCode = normalizeOptionalText("epigrafeAtCode", epigrafeAtCode, EPIGRAFE_AT_CODE_MAX_LENGTH);
    }

    public CompanyProfile update(
            String legalName,
            String taxIdentifier,
            String street,
            String city,
            String postalCode,
            String regionCode,
            String countryCode,
            String epigrafeAtCode
    ) {
        return new CompanyProfile(legalName, taxIdentifier, street, city, postalCode, regionCode, countryCode, epigrafeAtCode);
    }

    public String getLegalName()      { return legalName; }
    public String getTaxIdentifier()  { return taxIdentifier; }
    public String getStreet()         { return street; }
    public String getCity()           { return city; }
    public String getPostalCode()     { return postalCode; }
    public String getRegionCode()     { return regionCode; }
    public String getCountryCode()    { return countryCode; }
    public String getEpigrafeAtCode() { return epigrafeAtCode; }

    private String normalizeRequiredText(String fieldName, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        String normalized = value.trim();
        validateLength(fieldName, normalized, maxLength);
        return normalized;
    }

    private String normalizeOptionalText(String fieldName, String value, int maxLength) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        validateLength(fieldName, normalized, maxLength);
        return normalized;
    }

    private String normalizeOptionalCode(String value) {
        String normalized = normalizeOptionalText("countryCode", value, 3);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String normalizeOptionalCode(String fieldName, String value, int maxLength) {
        String normalized = normalizeOptionalText(fieldName, value, maxLength);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private void validateLength(String fieldName, String value, int maxLength) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }
    }
}
```

- [ ] **Step 4: Update UpsertCompanyProfileCommand**

```java
package com.b4rrhh.rulesystem.companyprofile.application.usecase;

public record UpsertCompanyProfileCommand(
        String ruleSystemCode,
        String companyCode,
        String legalName,
        String taxIdentifier,
        String street,
        String city,
        String postalCode,
        String regionCode,
        String countryCode,
        String epigrafeAtCode
) {
}
```

- [ ] **Step 5: Update UpsertCompanyProfileService — thread epigrafeAtCode**

In `UpsertCompanyProfileService.java`, update the `CompanyProfile` constructor call and the `existing.update(...)` call to include `command.epigrafeAtCode()` as the last argument in both places:

```java
CompanyProfile requestedProfile = new CompanyProfile(
        command.legalName(),
        command.taxIdentifier(),
        command.street(),
        command.city(),
        command.postalCode(),
        command.regionCode(),
        countryCode,
        command.epigrafeAtCode()   // add this
);

// ...existing upsert map remains the same, but update() now needs the extra arg:
CompanyProfile profileToSave = existingProfile
        .map(existing -> existing.update(
                requestedProfile.getLegalName(),
                requestedProfile.getTaxIdentifier(),
                requestedProfile.getStreet(),
                requestedProfile.getCity(),
                requestedProfile.getPostalCode(),
                requestedProfile.getRegionCode(),
                requestedProfile.getCountryCode(),
                requestedProfile.getEpigrafeAtCode()   // add this
        ))
        .orElse(requestedProfile);
```

- [ ] **Step 6: Update CompanyProfileEntity**

Add the field and getter/setter after `countryCode`:

```java
@Column(name = "epigrafe_at_code", length = 10)
private String epigrafeAtCode;

public String getEpigrafeAtCode()              { return epigrafeAtCode; }
public void setEpigrafeAtCode(String v)        { this.epigrafeAtCode = v; }
```

- [ ] **Step 7: Update CompanyProfilePersistenceAdapter**

In `save()`, add after `entity.setCountryCode(...)`:
```java
entity.setEpigrafeAtCode(companyProfile.getEpigrafeAtCode());
```

In `toDomain()`, update the constructor call:
```java
return new CompanyProfile(
        entity.getLegalName(),
        entity.getTaxIdentifier(),
        entity.getStreet(),
        entity.getCity(),
        entity.getPostalCode(),
        entity.getRegionCode(),
        entity.getCountryCode(),
        entity.getEpigrafeAtCode()   // add this
);
```

- [ ] **Step 8: Update UpsertCompanyProfileRequest**

```java
package com.b4rrhh.rulesystem.companyprofile.infrastructure.web.dto;

public record UpsertCompanyProfileRequest(
        String legalName,
        String taxIdentifier,
        CompanyProfileAddressRequest address,
        String epigrafeAtCode
) {
}
```

- [ ] **Step 9: Update CompanyProfileResponse**

```java
package com.b4rrhh.rulesystem.companyprofile.infrastructure.web.dto;

public record CompanyProfileResponse(
        String companyCode,
        String legalName,
        String taxIdentifier,
        CompanyProfileAddressResponse address,
        String epigrafeAtCode
) {
}
```

- [ ] **Step 10: Update CompanyProfileResponseAssembler**

```java
public CompanyProfileResponse toResponse(String companyCode, CompanyProfile companyProfile) {
    return new CompanyProfileResponse(
            normalizeRequiredCompanyCode(companyCode),
            companyProfile.getLegalName(),
            companyProfile.getTaxIdentifier(),
            new CompanyProfileAddressResponse(
                    companyProfile.getStreet(),
                    companyProfile.getCity(),
                    companyProfile.getPostalCode(),
                    companyProfile.getRegionCode(),
                    companyProfile.getCountryCode()
            ),
            companyProfile.getEpigrafeAtCode()   // add this
    );
}
```

- [ ] **Step 11: Fix all existing call sites broken by new constructor arity**

The existing tests and other classes construct `CompanyProfile(...)` with 7 args. Find all call sites and add `null` as the 8th argument:

```bash
grep -rn "new CompanyProfile(" src/ --include="*.java"
```

Update each call site: add `, null` before the closing `)` (the `epigrafeAtCode` param is optional).

Also fix all `existing.update(...)` call sites the same way (add `null` as last arg where not already updated).

- [ ] **Step 12: Fix the CompanyProfileController — map new request field to command**

In `CompanyProfileController.java`, find the `PUT` handler and add `request.epigrafeAtCode()` as the last argument when constructing `UpsertCompanyProfileCommand`.

- [ ] **Step 13: Run failing test — expect pass**

```bash
mvn test -Dtest=UpsertCompanyProfileServiceTest -q
```
Expected: BUILD SUCCESS, all tests green

- [ ] **Step 14: Run full test suite**

```bash
mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 15: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/companyprofile/ \
        src/test/java/com/b4rrhh/rulesystem/companyprofile/
git commit -m "feat: add epigrafe_at_code to CompanyProfile"
```

---

### Task 5: AgreementCategoryProfile domain layer

**Files:**
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/model/TipoNomina.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/model/AgreementCategoryProfile.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/port/AgreementCategoryProfileRepository.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/exception/AgreementCategoryProfileNotFoundException.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/exception/AgreementCategoryProfileCategoryNotFoundException.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/exception/GrupoCotizacionInvalidException.java`

- [ ] **Step 1: Create TipoNomina enum**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model;

public enum TipoNomina {
    MENSUAL,
    DIARIO
}
```

- [ ] **Step 2: Create AgreementCategoryProfile domain model**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model;

public class AgreementCategoryProfile {

    private static final int GRUPO_COTIZACION_MAX_LENGTH = 2;

    private final String grupoCotizacionCode;
    private final TipoNomina tipoNomina;

    public AgreementCategoryProfile(String grupoCotizacionCode, TipoNomina tipoNomina) {
        if (grupoCotizacionCode == null || grupoCotizacionCode.trim().isEmpty()) {
            throw new IllegalArgumentException("grupoCotizacionCode is required");
        }
        String normalized = grupoCotizacionCode.trim();
        if (normalized.length() > GRUPO_COTIZACION_MAX_LENGTH) {
            throw new IllegalArgumentException("grupoCotizacionCode exceeds max length " + GRUPO_COTIZACION_MAX_LENGTH);
        }
        if (tipoNomina == null) {
            throw new IllegalArgumentException("tipoNomina is required");
        }
        this.grupoCotizacionCode = normalized;
        this.tipoNomina = tipoNomina;
    }

    public String getGrupoCotizacionCode() { return grupoCotizacionCode; }
    public TipoNomina getTipoNomina()      { return tipoNomina; }
}
```

- [ ] **Step 3: Create AgreementCategoryProfileRepository port**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;

import java.util.Optional;

public interface AgreementCategoryProfileRepository {
    Optional<AgreementCategoryProfile> findByCategoryRuleEntityId(Long categoryRuleEntityId);
    AgreementCategoryProfile save(Long categoryRuleEntityId, AgreementCategoryProfile profile);
}
```

- [ ] **Step 4: Create exception classes**

```java
// AgreementCategoryProfileNotFoundException.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception;

public class AgreementCategoryProfileNotFoundException extends RuntimeException {
    public AgreementCategoryProfileNotFoundException(String ruleSystemCode, String categoryCode) {
        super("Agreement category profile not found for: " + ruleSystemCode + "/" + categoryCode);
    }
}
```

```java
// AgreementCategoryProfileCategoryNotFoundException.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception;

public class AgreementCategoryProfileCategoryNotFoundException extends RuntimeException {
    public AgreementCategoryProfileCategoryNotFoundException(String ruleSystemCode, String categoryCode) {
        super("Agreement category not found: " + ruleSystemCode + "/" + categoryCode);
    }
}
```

```java
// GrupoCotizacionInvalidException.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception;

public class GrupoCotizacionInvalidException extends RuntimeException {
    public GrupoCotizacionInvalidException(String ruleSystemCode, String grupoCotizacionCode) {
        super("Grupo de cotización not found: " + ruleSystemCode + "/" + grupoCotizacionCode);
    }
}
```

- [ ] **Step 5: Run tests**

```bash
mvn test -q
```
Expected: BUILD SUCCESS (no tests for pure domain yet, but compilation must succeed)

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/domain/
git commit -m "feat: AgreementCategoryProfile domain model, port, and exceptions"
```

---

### Task 6: [TDD] AgreementCategoryProfile use cases

**Files:**
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/GetAgreementCategoryProfileQuery.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/GetAgreementCategoryProfileUseCase.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/GetAgreementCategoryProfileService.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/UpsertAgreementCategoryProfileCommand.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/UpsertAgreementCategoryProfileUseCase.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/UpsertAgreementCategoryProfileService.java`
- Create: `src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/GetAgreementCategoryProfileServiceTest.java`
- Create: `src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/usecase/UpsertAgreementCategoryProfileServiceTest.java`

- [ ] **Step 1: Create query, command, and interface stubs**

```java
// GetAgreementCategoryProfileQuery.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

public record GetAgreementCategoryProfileQuery(String ruleSystemCode, String categoryCode) {}
```

```java
// GetAgreementCategoryProfileUseCase.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;

public interface GetAgreementCategoryProfileUseCase {
    AgreementCategoryProfile get(GetAgreementCategoryProfileQuery query);
}
```

```java
// UpsertAgreementCategoryProfileCommand.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

public record UpsertAgreementCategoryProfileCommand(
        String ruleSystemCode,
        String categoryCode,
        String grupoCotizacionCode,
        String tipoNomina
) {}
```

```java
// UpsertAgreementCategoryProfileUseCase.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;

public interface UpsertAgreementCategoryProfileUseCase {
    AgreementCategoryProfile upsert(UpsertAgreementCategoryProfileCommand command);
}
```

- [ ] **Step 2: Write failing tests**

```java
// GetAgreementCategoryProfileServiceTest.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgreementCategoryProfileServiceTest {

    @Mock private AgreementCategoryProfileRepository profileRepository;
    @Mock private RuleEntityRepository ruleEntityRepository;

    private GetAgreementCategoryProfileService service;

    @BeforeEach
    void setUp() {
        service = new GetAgreementCategoryProfileService(profileRepository, ruleEntityRepository);
    }

    @Test
    void returnsProfileWhenItExists() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(profileRepository.findByCategoryRuleEntityId(42L))
                .thenReturn(Optional.of(new AgreementCategoryProfile("05", TipoNomina.MENSUAL)));

        AgreementCategoryProfile result = service.get(new GetAgreementCategoryProfileQuery("ESP", "CAT_ADMIN"));

        assertEquals("05", result.getGrupoCotizacionCode());
        assertEquals(TipoNomina.MENSUAL, result.getTipoNomina());
    }

    @Test
    void throwsNotFoundWhenCategoryDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "UNKNOWN", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(ruleEntityRepository.findByFilters("ESP", "AGREEMENT_CATEGORY", "UNKNOWN", null, null))
                .thenReturn(java.util.List.of());

        assertThrows(AgreementCategoryProfileCategoryNotFoundException.class,
                () -> service.get(new GetAgreementCategoryProfileQuery("ESP", "UNKNOWN")));
    }

    @Test
    void throwsProfileNotFoundWhenProfileDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(profileRepository.findByCategoryRuleEntityId(42L))
                .thenReturn(Optional.empty());

        assertThrows(AgreementCategoryProfileNotFoundException.class,
                () -> service.get(new GetAgreementCategoryProfileQuery("ESP", "CAT_ADMIN")));
    }

    private RuleEntity ruleEntity(Long id, String ruleSystemCode, String typeCode, String code) {
        return new RuleEntity(id, ruleSystemCode, typeCode, code, code, null, true,
                LocalDate.of(1900, 1, 1), null, LocalDateTime.now(), LocalDateTime.now());
    }
}
```

```java
// UpsertAgreementCategoryProfileServiceTest.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertAgreementCategoryProfileServiceTest {

    @Mock private AgreementCategoryProfileRepository profileRepository;
    @Mock private RuleEntityRepository ruleEntityRepository;

    private UpsertAgreementCategoryProfileService service;

    @BeforeEach
    void setUp() {
        service = new UpsertAgreementCategoryProfileService(profileRepository, ruleEntityRepository);
    }

    @Test
    void createsProfileWhenItDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "05", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(99L, "ESP", "GRUPO_COTIZACION", "05")));
        when(profileRepository.findByCategoryRuleEntityId(42L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(Long.class), any(AgreementCategoryProfile.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        AgreementCategoryProfile result = service.upsert(
                new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "05", "MENSUAL"));

        ArgumentCaptor<AgreementCategoryProfile> captor = ArgumentCaptor.forClass(AgreementCategoryProfile.class);
        verify(profileRepository).save(eq(42L), captor.capture());
        assertEquals("05", captor.getValue().getGrupoCotizacionCode());
        assertEquals(TipoNomina.MENSUAL, captor.getValue().getTipoNomina());
        assertEquals("05", result.getGrupoCotizacionCode());
    }

    @Test
    void updatesExistingProfile() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "07", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(88L, "ESP", "GRUPO_COTIZACION", "07")));
        when(profileRepository.findByCategoryRuleEntityId(42L))
                .thenReturn(Optional.of(new AgreementCategoryProfile("05", TipoNomina.MENSUAL)));
        when(profileRepository.save(any(Long.class), any(AgreementCategoryProfile.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        AgreementCategoryProfile result = service.upsert(
                new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "07", "MENSUAL"));

        assertEquals("07", result.getGrupoCotizacionCode());
    }

    @Test
    void throwsWhenCategoryNotFound() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "GHOST", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(ruleEntityRepository.findByFilters("ESP", "AGREEMENT_CATEGORY", "GHOST", null, null))
                .thenReturn(java.util.List.of());

        assertThrows(AgreementCategoryProfileCategoryNotFoundException.class,
                () -> service.upsert(new UpsertAgreementCategoryProfileCommand("ESP", "GHOST", "05", "MENSUAL")));
    }

    @Test
    void throwsWhenGrupoCotizacionNotFound() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "99", LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThrows(GrupoCotizacionInvalidException.class,
                () -> service.upsert(new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "99", "MENSUAL")));
    }

    @Test
    void throwsWhenTipoNominaIsInvalid() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "05", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(99L, "ESP", "GRUPO_COTIZACION", "05")));

        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "05", "SEMANAL")));
    }

    private RuleEntity ruleEntity(Long id, String ruleSystemCode, String typeCode, String code) {
        return new RuleEntity(id, ruleSystemCode, typeCode, code, code, null, true,
                LocalDate.of(1900, 1, 1), null, LocalDateTime.now(), LocalDateTime.now());
    }
}
```

- [ ] **Step 3: Run tests — expect compile errors (services don't exist yet)**

```bash
mvn test -Dtest="GetAgreementCategoryProfileServiceTest,UpsertAgreementCategoryProfileServiceTest" -q 2>&1 | head -20
```
Expected: compile error — service classes not found

- [ ] **Step 4: Implement GetAgreementCategoryProfileService**

The service resolves the category rule entity using `RuleEntityRepository` (same port used by companyprofile services). When the category has no applicable occurrence today but exists historically, it should throw `AgreementCategoryProfileCategoryNotFoundException`.

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GetAgreementCategoryProfileService implements GetAgreementCategoryProfileUseCase {

    private final AgreementCategoryProfileRepository profileRepository;
    private final RuleEntityRepository ruleEntityRepository;

    public GetAgreementCategoryProfileService(
            AgreementCategoryProfileRepository profileRepository,
            RuleEntityRepository ruleEntityRepository
    ) {
        this.profileRepository = profileRepository;
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgreementCategoryProfile get(GetAgreementCategoryProfileQuery query) {
        String ruleSystemCode = query.ruleSystemCode().trim().toUpperCase();
        String categoryCode   = query.categoryCode().trim().toUpperCase();

        RuleEntity category = ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, LocalDate.now())
                .orElseThrow(() -> {
                    boolean exists = !ruleEntityRepository.findByFilters(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, null, null).isEmpty();
                    return new AgreementCategoryProfileCategoryNotFoundException(ruleSystemCode, categoryCode);
                });

        return profileRepository.findByCategoryRuleEntityId(category.getId())
                .orElseThrow(() -> new AgreementCategoryProfileNotFoundException(ruleSystemCode, categoryCode));
    }
}
```

- [ ] **Step 5: Implement UpsertAgreementCategoryProfileService**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UpsertAgreementCategoryProfileService implements UpsertAgreementCategoryProfileUseCase {

    private final AgreementCategoryProfileRepository profileRepository;
    private final RuleEntityRepository ruleEntityRepository;

    public UpsertAgreementCategoryProfileService(
            AgreementCategoryProfileRepository profileRepository,
            RuleEntityRepository ruleEntityRepository
    ) {
        this.profileRepository = profileRepository;
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    @Transactional
    public AgreementCategoryProfile upsert(UpsertAgreementCategoryProfileCommand command) {
        String ruleSystemCode      = command.ruleSystemCode().trim().toUpperCase();
        String categoryCode        = command.categoryCode().trim().toUpperCase();
        String grupoCotizacionCode = command.grupoCotizacionCode().trim();
        TipoNomina tipoNomina      = parseTipoNomina(command.tipoNomina());

        RuleEntity category = ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, LocalDate.now())
                .orElseThrow(() -> {
                    ruleEntityRepository.findByFilters(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, null, null);
                    return new AgreementCategoryProfileCategoryNotFoundException(ruleSystemCode, categoryCode);
                });

        ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, "GRUPO_COTIZACION", grupoCotizacionCode, LocalDate.now())
                .orElseThrow(() -> new GrupoCotizacionInvalidException(ruleSystemCode, grupoCotizacionCode));

        AgreementCategoryProfile requested = new AgreementCategoryProfile(grupoCotizacionCode, tipoNomina);

        Optional<AgreementCategoryProfile> existing = profileRepository.findByCategoryRuleEntityId(category.getId());
        AgreementCategoryProfile toSave = existing
                .map(e -> new AgreementCategoryProfile(requested.getGrupoCotizacionCode(), requested.getTipoNomina()))
                .orElse(requested);

        return profileRepository.save(category.getId(), toSave);
    }

    private TipoNomina parseTipoNomina(String value) {
        if (value == null) throw new IllegalArgumentException("tipoNomina is required");
        try {
            return TipoNomina.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipoNomina must be MENSUAL or DIARIO, got: " + value);
        }
    }
}
```

- [ ] **Step 6: Run tests — expect all pass**

```bash
mvn test -Dtest="GetAgreementCategoryProfileServiceTest,UpsertAgreementCategoryProfileServiceTest" -q
```
Expected: BUILD SUCCESS, 8 tests green

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/ \
        src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/application/
git commit -m "feat: AgreementCategoryProfile use cases with TDD"
```

---

### Task 7: AgreementCategoryProfile persistence layer

**Files:**
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/persistence/AgreementCategoryProfileEntity.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/persistence/SpringDataAgreementCategoryProfileRepository.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/persistence/AgreementCategoryProfilePersistenceAdapter.java`

- [ ] **Step 1: Create JPA entity**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "agreement_category_profile",
        schema = "rulesystem",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_agreement_category_profile",
                columnNames = "agreement_category_rule_entity_id"
        )
)
public class AgreementCategoryProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agreement_category_rule_entity_id", nullable = false)
    private Long agreementCategoryRuleEntityId;

    @Column(name = "grupo_cotizacion_code", nullable = false, length = 2)
    private String grupoCotizacionCode;

    @Column(name = "tipo_nomina", nullable = false, length = 10)
    private String tipoNomina;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId()                                { return id; }
    public void setId(Long id)                         { this.id = id; }
    public Long getAgreementCategoryRuleEntityId()     { return agreementCategoryRuleEntityId; }
    public void setAgreementCategoryRuleEntityId(Long v) { this.agreementCategoryRuleEntityId = v; }
    public String getGrupoCotizacionCode()             { return grupoCotizacionCode; }
    public void setGrupoCotizacionCode(String v)       { this.grupoCotizacionCode = v; }
    public String getTipoNomina()                      { return tipoNomina; }
    public void setTipoNomina(String v)                { this.tipoNomina = v; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void setCreatedAt(LocalDateTime v)          { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)          { this.updatedAt = v; }
}
```

- [ ] **Step 2: Create Spring Data repository**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataAgreementCategoryProfileRepository
        extends JpaRepository<AgreementCategoryProfileEntity, Long> {

    Optional<AgreementCategoryProfileEntity> findByAgreementCategoryRuleEntityId(Long agreementCategoryRuleEntityId);
}
```

- [ ] **Step 3: Create persistence adapter**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.persistence;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AgreementCategoryProfilePersistenceAdapter implements AgreementCategoryProfileRepository {

    private final SpringDataAgreementCategoryProfileRepository springDataRepository;

    public AgreementCategoryProfilePersistenceAdapter(
            SpringDataAgreementCategoryProfileRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<AgreementCategoryProfile> findByCategoryRuleEntityId(Long categoryRuleEntityId) {
        return springDataRepository.findByAgreementCategoryRuleEntityId(categoryRuleEntityId)
                .map(this::toDomain);
    }

    @Override
    public AgreementCategoryProfile save(Long categoryRuleEntityId, AgreementCategoryProfile profile) {
        AgreementCategoryProfileEntity entity = springDataRepository
                .findByAgreementCategoryRuleEntityId(categoryRuleEntityId)
                .orElseGet(AgreementCategoryProfileEntity::new);

        entity.setAgreementCategoryRuleEntityId(categoryRuleEntityId);
        entity.setGrupoCotizacionCode(profile.getGrupoCotizacionCode());
        entity.setTipoNomina(profile.getTipoNomina().name());

        return toDomain(springDataRepository.save(entity));
    }

    private AgreementCategoryProfile toDomain(AgreementCategoryProfileEntity entity) {
        return new AgreementCategoryProfile(
                entity.getGrupoCotizacionCode(),
                TipoNomina.valueOf(entity.getTipoNomina())
        );
    }
}
```

- [ ] **Step 4: Run tests**

```bash
mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/persistence/
git commit -m "feat: AgreementCategoryProfile persistence adapter"
```

---

### Task 8: [TDD] AgreementCategoryProfile web layer

**Files:**
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/dto/UpsertAgreementCategoryProfileRequest.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/dto/AgreementCategoryProfileResponse.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/dto/AgreementCategoryProfileErrorResponse.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/assembler/AgreementCategoryProfileResponseAssembler.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/AgreementCategoryProfileController.java`
- Create: `src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/AgreementCategoryProfileExceptionHandler.java`
- Create: `src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/AgreementCategoryProfileControllerHttpTest.java`

- [ ] **Step 1: Write failing controller test**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web;

import com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase.*;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.assembler.AgreementCategoryProfileResponseAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgreementCategoryProfileControllerHttpTest {

    @Mock private GetAgreementCategoryProfileUseCase getUseCase;
    @Mock private UpsertAgreementCategoryProfileUseCase upsertUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AgreementCategoryProfileController controller = new AgreementCategoryProfileController(
                getUseCase, upsertUseCase, new AgreementCategoryProfileResponseAssembler());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AgreementCategoryProfileExceptionHandler())
                .build();
    }

    @Test
    void getReturnsExistingProfile() throws Exception {
        when(getUseCase.get(any())).thenReturn(new AgreementCategoryProfile("05", TipoNomina.MENSUAL));

        mockMvc.perform(get("/agreement-categories/ESP/CAT_ADMIN/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryCode").value("CAT_ADMIN"))
                .andExpect(jsonPath("$.grupoCotizacionCode").value("05"))
                .andExpect(jsonPath("$.tipoNomina").value("MENSUAL"));
    }

    @Test
    void putMapsPathAndBodyToCommand() throws Exception {
        when(upsertUseCase.upsert(any())).thenReturn(new AgreementCategoryProfile("03", TipoNomina.MENSUAL));

        mockMvc.perform(put("/agreement-categories/ESP/CAT_ADMIN/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grupoCotizacionCode": "03", "tipoNomina": "MENSUAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupoCotizacionCode").value("03"));

        ArgumentCaptor<UpsertAgreementCategoryProfileCommand> captor =
                ArgumentCaptor.forClass(UpsertAgreementCategoryProfileCommand.class);
        verify(upsertUseCase).upsert(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("CAT_ADMIN", captor.getValue().categoryCode());
        assertEquals("03", captor.getValue().grupoCotizacionCode());
    }

    @Test
    void getMapsCategoryNotFoundToHttp404() throws Exception {
        when(getUseCase.get(any())).thenThrow(new AgreementCategoryProfileCategoryNotFoundException("ESP", "GHOST"));

        mockMvc.perform(get("/agreement-categories/ESP/GHOST/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Agreement category not found")));
    }

    @Test
    void getMapsProfileNotFoundToHttp404() throws Exception {
        when(getUseCase.get(any())).thenThrow(new AgreementCategoryProfileNotFoundException("ESP", "CAT_ADMIN"));

        mockMvc.perform(get("/agreement-categories/ESP/CAT_ADMIN/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("profile not found")));
    }

    @Test
    void putMapsGrupoCotizacionInvalidToHttp422() throws Exception {
        when(upsertUseCase.upsert(any())).thenThrow(new GrupoCotizacionInvalidException("ESP", "99"));

        mockMvc.perform(put("/agreement-categories/ESP/CAT_ADMIN/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grupoCotizacionCode": "99", "tipoNomina": "MENSUAL"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Grupo de cotización not found")));
    }
}
```

- [ ] **Step 2: Run test — expect compile error (controller and related classes don't exist)**

```bash
mvn test -Dtest=AgreementCategoryProfileControllerHttpTest -q 2>&1 | head -20
```
Expected: compile error

- [ ] **Step 3: Create DTOs**

```java
// UpsertAgreementCategoryProfileRequest.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto;

public record UpsertAgreementCategoryProfileRequest(
        String grupoCotizacionCode,
        String tipoNomina
) {}
```

```java
// AgreementCategoryProfileResponse.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto;

public record AgreementCategoryProfileResponse(
        String categoryCode,
        String grupoCotizacionCode,
        String tipoNomina
) {}
```

```java
// AgreementCategoryProfileErrorResponse.java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto;

public record AgreementCategoryProfileErrorResponse(String message) {}
```

- [ ] **Step 4: Create assembler**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.assembler;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.AgreementCategoryProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class AgreementCategoryProfileResponseAssembler {

    public AgreementCategoryProfileResponse toResponse(String categoryCode, AgreementCategoryProfile profile) {
        return new AgreementCategoryProfileResponse(
                categoryCode.trim().toUpperCase(),
                profile.getGrupoCotizacionCode(),
                profile.getTipoNomina().name()
        );
    }
}
```

- [ ] **Step 5: Create controller**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web;

import com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase.*;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.assembler.AgreementCategoryProfileResponseAssembler;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.AgreementCategoryProfileResponse;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.UpsertAgreementCategoryProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agreement-categories/{ruleSystemCode}/{categoryCode}/profile")
public class AgreementCategoryProfileController {

    private final GetAgreementCategoryProfileUseCase getUseCase;
    private final UpsertAgreementCategoryProfileUseCase upsertUseCase;
    private final AgreementCategoryProfileResponseAssembler assembler;

    public AgreementCategoryProfileController(
            GetAgreementCategoryProfileUseCase getUseCase,
            UpsertAgreementCategoryProfileUseCase upsertUseCase,
            AgreementCategoryProfileResponseAssembler assembler
    ) {
        this.getUseCase = getUseCase;
        this.upsertUseCase = upsertUseCase;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<AgreementCategoryProfileResponse> get(
            @PathVariable String ruleSystemCode,
            @PathVariable String categoryCode
    ) {
        AgreementCategoryProfile profile = getUseCase.get(
                new GetAgreementCategoryProfileQuery(ruleSystemCode, categoryCode));
        return ResponseEntity.ok(assembler.toResponse(categoryCode, profile));
    }

    @PutMapping
    public ResponseEntity<AgreementCategoryProfileResponse> upsert(
            @PathVariable String ruleSystemCode,
            @PathVariable String categoryCode,
            @RequestBody UpsertAgreementCategoryProfileRequest request
    ) {
        AgreementCategoryProfile profile = upsertUseCase.upsert(
                new UpsertAgreementCategoryProfileCommand(
                        ruleSystemCode,
                        categoryCode,
                        request.grupoCotizacionCode(),
                        request.tipoNomina()
                ));
        return ResponseEntity.ok(assembler.toResponse(categoryCode, profile));
    }
}
```

- [ ] **Step 6: Create exception handler**

```java
package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.AgreementCategoryProfileErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AgreementCategoryProfileExceptionHandler {

    @ExceptionHandler(AgreementCategoryProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AgreementCategoryProfileErrorResponse handleProfileNotFound(AgreementCategoryProfileNotFoundException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AgreementCategoryProfileCategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AgreementCategoryProfileErrorResponse handleCategoryNotFound(AgreementCategoryProfileCategoryNotFoundException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(GrupoCotizacionInvalidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public AgreementCategoryProfileErrorResponse handleGrupoInvalid(GrupoCotizacionInvalidException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AgreementCategoryProfileErrorResponse handleBadRequest(IllegalArgumentException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }
}
```

- [ ] **Step 7: Run controller tests — expect all pass**

```bash
mvn test -Dtest=AgreementCategoryProfileControllerHttpTest -q
```
Expected: BUILD SUCCESS, 5 tests green

- [ ] **Step 8: Run full test suite**

```bash
mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/b4rrhh/rulesystem/agreementcategoryprofile/infrastructure/web/ \
        src/test/java/com/b4rrhh/rulesystem/agreementcategoryprofile/
git commit -m "feat: AgreementCategoryProfile web layer with controller tests"
```

---

### Task 9: OpenAPI spec updates

**Files:**
- Modify: `openapi/personnel-administration-api.yaml`

- [ ] **Step 1: Add `epigrafeAtCode` to company profile schemas**

In the YAML, find `UpsertCompanyProfileRequest` schema and add:
```yaml
      epigrafeAtCode:
        type: string
        maxLength: 10
        nullable: true
        description: "Epígrafe AT/EP de la empresa ante la TGSS (código de tarifa de accidentes de trabajo)"
        example: "6210"
```

Find `CompanyProfileResponse` schema and add:
```yaml
      epigrafeAtCode:
        type: string
        nullable: true
        description: "Epígrafe AT/EP registrado ante la TGSS"
        example: "6210"
```

- [ ] **Step 2: Add new AgreementCategoryProfile schemas**

Find the `components.schemas` section and add:

```yaml
    UpsertAgreementCategoryProfileRequest:
      type: object
      required:
        - grupoCotizacionCode
        - tipoNomina
      properties:
        grupoCotizacionCode:
          type: string
          maxLength: 2
          description: "Grupo de cotización SS (01–11)"
          example: "05"
        tipoNomina:
          type: string
          enum: [MENSUAL, DIARIO]
          description: "Tipo de base de cotización: mensual (grupos 1-7) o diaria (grupos 8-11)"
          example: "MENSUAL"

    AgreementCategoryProfileResponse:
      type: object
      properties:
        categoryCode:
          type: string
        grupoCotizacionCode:
          type: string
          description: "Grupo de cotización SS (01–11)"
        tipoNomina:
          type: string
          enum: [MENSUAL, DIARIO]

    AgreementCategoryProfileErrorResponse:
      type: object
      properties:
        message:
          type: string
```

- [ ] **Step 3: Add new AgreementCategoryProfile endpoints**

Find the `paths` section and add (after the last agreement-related path or in alphabetical order):

```yaml
  /agreement-categories/{ruleSystemCode}/{categoryCode}/profile:
    get:
      tags:
        - Agreement Category Profile
      summary: Get SS cotización profile for an agreement category
      operationId: getAgreementCategoryProfile
      parameters:
        - name: ruleSystemCode
          in: path
          required: true
          schema:
            type: string
          example: "ESP"
        - name: categoryCode
          in: path
          required: true
          schema:
            type: string
          example: "CAT_ADMIN"
      responses:
        "200":
          description: Profile found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AgreementCategoryProfileResponse'
        "404":
          description: Category or profile not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AgreementCategoryProfileErrorResponse'
    put:
      tags:
        - Agreement Category Profile
      summary: Create or update SS cotización profile for an agreement category
      operationId: upsertAgreementCategoryProfile
      parameters:
        - name: ruleSystemCode
          in: path
          required: true
          schema:
            type: string
          example: "ESP"
        - name: categoryCode
          in: path
          required: true
          schema:
            type: string
          example: "CAT_ADMIN"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UpsertAgreementCategoryProfileRequest'
      responses:
        "200":
          description: Profile created or updated
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AgreementCategoryProfileResponse'
        "404":
          description: Category not found
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AgreementCategoryProfileErrorResponse'
        "422":
          description: Grupo de cotización invalid
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AgreementCategoryProfileErrorResponse'
```

- [ ] **Step 4: Run full test suite**

```bash
mvn test -q
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add openapi/personnel-administration-api.yaml
git commit -m "feat: OpenAPI spec — AgreementCategoryProfile endpoints and epigrafeAtCode on company profile"
```

---

## Self-Review

### Spec coverage check

| Requirement | Covered by |
|-------------|-----------|
| Seed GRUPO_COTIZACION catalog (01-11) | Task 1 |
| agreement_category_profile table | Task 2 |
| epigrafe_at_code on company_profile | Task 3 |
| Seed profiles for existing ESP mock categories | Task 3 |
| CompanyProfile domain + API supports epigrafeAtCode | Task 4 |
| AgreementCategoryProfile domain model + port + exceptions | Task 5 |
| GetAgreementCategoryProfile use case with TDD | Task 6 |
| UpsertAgreementCategoryProfile use case with TDD | Task 6 |
| Persistence adapter (entity + Spring Data + adapter) | Task 7 |
| Web layer (controller + DTOs + assembler + exception handler) | Task 8 |
| Controller tests (5 scenarios) | Task 8 |
| OpenAPI spec for all new endpoints and schema changes | Task 9 |

### No placeholders found — all code is complete.

### Type consistency check
- `AgreementCategoryProfile` constructor takes `(String grupoCotizacionCode, TipoNomina tipoNomina)` — consistent across domain, service, persistence adapter.
- `TipoNomina` enum used in domain model, stored as String in JPA entity, serialized as String in API response.
- `RuleEntityRepository.findApplicableByBusinessKey(ruleSystemCode, typeCode, code, date)` signature used consistently across both service tests and implementations.
- `UpsertAgreementCategoryProfileCommand` record fields match exactly what the controller constructs and the service reads.
