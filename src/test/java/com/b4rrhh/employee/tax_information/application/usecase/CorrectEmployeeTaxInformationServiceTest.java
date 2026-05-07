package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationNotFoundException;
import com.b4rrhh.employee.tax_information.domain.model.*;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrectEmployeeTaxInformationServiceTest {

    @Mock EmployeeTaxInformationRepository repo;
    @Mock EmployeeForTaxInfoLookupPort employeeLookupPort;

    private CorrectEmployeeTaxInformationService service;

    @BeforeEach
    void setUp() {
        service = new CorrectEmployeeTaxInformationService(repo, employeeLookupPort);
    }

    @Test
    void correct_updatesFields_preservingValidFrom() {
        when(employeeLookupPort.findEmployeeId(any(),any(),any())).thenReturn(Optional.of(42L));
        var existing = EmployeeTaxInformation.rehydrate(7L, 42L, LocalDate.of(2025,1,1),
            FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN,
            LocalDateTime.now(), LocalDateTime.now());
        when(repo.findByEmployeeIdAndValidFrom(42L, LocalDate.of(2025,1,1)))
            .thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var cmd = new CorrectEmployeeTaxInformationCommand("ESP","INTERNAL","EMP001",
            LocalDate.of(2025,1,1), FamilySituation.MARRIED_DEPENDENT_SPOUSE, 2, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.BIZKAIA);

        var result = service.correct(cmd);

        assertEquals(7L, result.getId());
        assertEquals(LocalDate.of(2025,1,1), result.getValidFrom());
        assertEquals(FamilySituation.MARRIED_DEPENDENT_SPOUSE, result.getFamilySituation());
        assertEquals(TaxTerritory.BIZKAIA, result.getTaxTerritory());
    }

    @Test
    void correct_throwsNotFound_whenRecordMissing() {
        when(employeeLookupPort.findEmployeeId(any(),any(),any())).thenReturn(Optional.of(42L));
        when(repo.findByEmployeeIdAndValidFrom(any(), any())).thenReturn(Optional.empty());
        var cmd = new CorrectEmployeeTaxInformationCommand("ESP","INTERNAL","EMP001",
            LocalDate.of(2025,1,1), FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN);
        assertThrows(EmployeeTaxInformationNotFoundException.class, () -> service.correct(cmd));
    }
}
