package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.application.port.TaxInfoPresenceLookupPort;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationAlreadyExistsException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationEmployeeNotFoundException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationInvalidValidFromException;
import com.b4rrhh.employee.tax_information.domain.model.*;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEmployeeTaxInformationServiceTest {

    @Mock EmployeeTaxInformationRepository repo;
    @Mock EmployeeForTaxInfoLookupPort employeeLookupPort;
    @Mock TaxInfoPresenceLookupPort presenceLookupPort;

    private CreateEmployeeTaxInformationService service;

    @BeforeEach
    void setUp() {
        service = new CreateEmployeeTaxInformationService(repo, employeeLookupPort, presenceLookupPort);
    }

    private CreateEmployeeTaxInformationCommand cmdForFirstOfMonth() {
        return new CreateEmployeeTaxInformationCommand("ESP", "INTERNAL", "EMP001",
            LocalDate.of(2025,1,1), FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN);
    }

    @Test
    void create_savesRecord_whenValidFromIsFirstOfMonth() {
        when(employeeLookupPort.findEmployeeId("ESP","INTERNAL","EMP001")).thenReturn(Optional.of(42L));
        when(repo.findByEmployeeIdAndValidFrom(any(), any())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.create(cmdForFirstOfMonth());

        assertEquals(FamilySituation.SINGLE_OR_OTHER, result.getFamilySituation());
        verify(repo).save(any());
    }

    @Test
    void create_throwsEmployeeNotFound_whenEmployeeMissing() {
        when(employeeLookupPort.findEmployeeId(any(),any(),any())).thenReturn(Optional.empty());
        assertThrows(EmployeeTaxInformationEmployeeNotFoundException.class,
            () -> service.create(cmdForFirstOfMonth()));
        verify(repo, never()).save(any());
    }

    @Test
    void create_throwsAlreadyExists_whenDuplicateValidFrom() {
        when(employeeLookupPort.findEmployeeId(any(),any(),any())).thenReturn(Optional.of(42L));
        when(repo.findByEmployeeIdAndValidFrom(any(), any())).thenReturn(Optional.of(EmployeeTaxInformation.DEFAULT));
        assertThrows(EmployeeTaxInformationAlreadyExistsException.class,
            () -> service.create(cmdForFirstOfMonth()));
    }

    @Test
    void create_throwsInvalidValidFrom_whenMidMonthAndNotPresenceStart() {
        when(employeeLookupPort.findEmployeeId(any(),any(),any())).thenReturn(Optional.of(42L));
        when(presenceLookupPort.isPresenceStartDate(42L, LocalDate.of(2025,1,15))).thenReturn(false);

        var cmd = new CreateEmployeeTaxInformationCommand("ESP","INTERNAL","EMP001",
            LocalDate.of(2025,1,15), FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN);
        assertThrows(EmployeeTaxInformationInvalidValidFromException.class, () -> service.create(cmd));
    }

    @Test
    void create_allowsMidMonthValidFrom_whenMatchesPresenceStart() {
        when(employeeLookupPort.findEmployeeId(any(),any(),any())).thenReturn(Optional.of(42L));
        when(presenceLookupPort.isPresenceStartDate(42L, LocalDate.of(2025,1,15))).thenReturn(true);
        when(repo.findByEmployeeIdAndValidFrom(any(), any())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var cmd = new CreateEmployeeTaxInformationCommand("ESP","INTERNAL","EMP001",
            LocalDate.of(2025,1,15), FamilySituation.SINGLE_OR_OTHER, 0, 0,
            DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN);
        assertDoesNotThrow(() -> service.create(cmd));
    }
}
