package com.b4rrhh.employee.tax_information.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTaxInformationTest {

    @Test
    void create_buildsCorrectInstance() {
        var result = EmployeeTaxInformation.create(1L, LocalDate.of(2025,1,1),
            FamilySituation.SINGLE_OR_OTHER, 2, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN);
        assertNull(result.getId());
        assertEquals(FamilySituation.SINGLE_OR_OTHER, result.getFamilySituation());
        assertEquals(2, result.getDescendantsCount());
        assertEquals(TaxTerritory.COMUN, result.getTaxTerritory());
    }

    @Test
    void correct_returnsNewInstancePreservingIdentity() {
        var original = EmployeeTaxInformation.rehydrate(7L, 1L, LocalDate.of(2025,1,1),
            FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN, null, null);
        var corrected = original.correct(
            FamilySituation.MARRIED_DEPENDENT_SPOUSE, 3, 1,
            DisabilityDegree.MODERATE, true, false, true, TaxTerritory.BIZKAIA);
        assertEquals(7L, corrected.getId());
        assertEquals(LocalDate.of(2025,1,1), corrected.getValidFrom());
        assertEquals(FamilySituation.MARRIED_DEPENDENT_SPOUSE, corrected.getFamilySituation());
        assertEquals(TaxTerritory.BIZKAIA, corrected.getTaxTerritory());
    }

    @Test
    void create_throwsOnNegativeCounts() {
        assertThrows(IllegalArgumentException.class, () ->
            EmployeeTaxInformation.create(1L, LocalDate.of(2025,1,1),
                FamilySituation.SINGLE_OR_OTHER, -1, 0,
                DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN));
    }

    @Test
    void default_hasExpectedValues() {
        assertEquals(FamilySituation.SINGLE_OR_OTHER, EmployeeTaxInformation.DEFAULT.getFamilySituation());
        assertEquals(TaxTerritory.COMUN, EmployeeTaxInformation.DEFAULT.getTaxTerritory());
        assertEquals(DisabilityDegree.NONE, EmployeeTaxInformation.DEFAULT.getDisabilityDegree());
    }

    @Test
    void create_throwsOnNegativeAscendantsCount() {
        assertThrows(IllegalArgumentException.class, () ->
            EmployeeTaxInformation.create(1L, LocalDate.of(2025,1,1),
                FamilySituation.SINGLE_OR_OTHER, 0, -1,
                DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN));
    }

    @Test
    void correct_throwsOnNegativeCounts() {
        var original = EmployeeTaxInformation.rehydrate(7L, 1L, LocalDate.of(2025,1,1),
            FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN, null, null);
        assertThrows(IllegalArgumentException.class, () ->
            original.correct(FamilySituation.SINGLE_OR_OTHER, -1, 0,
                DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN));
    }

    @Test
    void correct_preservesCreatedAt() {
        var now = java.time.LocalDateTime.of(2025, 1, 1, 10, 0);
        var original = EmployeeTaxInformation.rehydrate(7L, 1L, LocalDate.of(2025,1,1),
            FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN, now, now);
        var corrected = original.correct(FamilySituation.MARRIED_DEPENDENT_SPOUSE, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN);
        assertEquals(now, corrected.getCreatedAt());
        assertNull(corrected.getUpdatedAt());
    }

    @Test
    void create_throwsOnNullRequiredFields() {
        assertThrows(IllegalArgumentException.class, () ->
            EmployeeTaxInformation.create(null, LocalDate.of(2025,1,1),
                FamilySituation.SINGLE_OR_OTHER, 0, 0,
                DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN));
        assertThrows(IllegalArgumentException.class, () ->
            EmployeeTaxInformation.create(1L, null,
                FamilySituation.SINGLE_OR_OTHER, 0, 0,
                DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN));
        assertThrows(IllegalArgumentException.class, () ->
            EmployeeTaxInformation.create(1L, LocalDate.of(2025,1,1),
                null, 0, 0, DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN));
    }
}
