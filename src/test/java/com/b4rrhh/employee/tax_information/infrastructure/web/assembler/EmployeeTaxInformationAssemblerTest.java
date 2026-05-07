package com.b4rrhh.employee.tax_information.infrastructure.web.assembler;

import com.b4rrhh.employee.tax_information.domain.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeTaxInformationAssemblerTest {

    private final EmployeeTaxInformationAssembler assembler = new EmployeeTaxInformationAssembler();

    @Test
    void toResponse_mapsAllFields() {
        var domain = EmployeeTaxInformation.rehydrate(1L, 42L, LocalDate.of(2025,1,1),
            FamilySituation.MARRIED_DEPENDENT_SPOUSE, 2, 1,
            DisabilityDegree.MODERATE, true, false, true, TaxTerritory.BIZKAIA,
            LocalDateTime.now(), LocalDateTime.now());

        var response = assembler.toResponse(domain);

        assertEquals("2025-01-01", response.validFrom());
        assertEquals("MARRIED_DEPENDENT_SPOUSE", response.familySituation());
        assertEquals(2, response.descendantsCount());
        assertEquals(1, response.ascendantsCount());
        assertEquals("MODERATE", response.disabilityDegree());
        assertTrue(response.pensionCompensatoria());
        assertFalse(response.geographicMobility());
        assertTrue(response.habitualResidenceLoan());
        assertEquals("BIZKAIA", response.taxTerritory());
    }
}
