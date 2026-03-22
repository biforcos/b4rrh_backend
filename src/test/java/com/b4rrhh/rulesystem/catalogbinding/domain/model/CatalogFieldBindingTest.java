package com.b4rrhh.rulesystem.catalogbinding.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogFieldBindingTest {

    @Test
    void acceptsValidDirectBinding() {
        assertDoesNotThrow(() -> new CatalogFieldBinding(
                "employee.work_center",
                "workCenterCode",
                CatalogKind.DIRECT,
                "WORK_CENTER",
                null,
                null,
                true
        ));
    }

    @Test
    void rejectsDependentWithoutDependsOnFieldCode() {
        assertThrows(IllegalArgumentException.class, () -> new CatalogFieldBinding(
                "employee.labor_classification",
                "agreementCategoryCode",
                CatalogKind.DEPENDENT,
                "AGREEMENT_CATEGORY",
                null,
                null,
                true
        ));
    }

    @Test
    void rejectsCustomWhenRuleEntityTypeCodeIsPresent() {
        assertThrows(IllegalArgumentException.class, () -> new CatalogFieldBinding(
                "employee.custom",
                "customFieldCode",
                CatalogKind.CUSTOM,
                "WORK_CENTER",
                null,
                "CUSTOM_RESOLVER",
                true
        ));
    }

    @Test
    void rejectsCustomWhenDependsOnFieldCodeIsPresent() {
        assertThrows(IllegalArgumentException.class, () -> new CatalogFieldBinding(
                "employee.custom",
                "customFieldCode",
                CatalogKind.CUSTOM,
                null,
                "agreementCode",
                "CUSTOM_RESOLVER",
                true
        ));
    }
}