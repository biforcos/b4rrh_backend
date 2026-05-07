package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.employee.tax_information.domain.model.DisabilityDegree;
import com.b4rrhh.employee.tax_information.domain.model.FamilySituation;
import com.b4rrhh.employee.tax_information.domain.model.TaxTerritory;
import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.infrastructure.persistence.EmployeeTaxInformationEntity;
import com.b4rrhh.employee.tax_information.infrastructure.persistence.SpringDataEmployeeTaxInformationRepository;
import com.b4rrhh.payroll.application.port.EmployeeTaxInfoContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeTaxInfoPayrollLookupAdapterTest {

    @Mock SpringDataEmployeeTaxInformationRepository springDataRepo;
    @Mock EmployeeForTaxInfoLookupPort employeeLookupAdapter;
    @InjectMocks EmployeeTaxInfoPayrollLookupAdapter adapter;

    @Test
    void returnsDefault_whenEmployeeNotFound() {
        when(employeeLookupAdapter.findEmployeeId(any(), any(), any())).thenReturn(Optional.empty());

        EmployeeTaxInfoContext result = adapter.findLatestOnOrBefore("ESP", "INTERNAL", "EMP001", LocalDate.of(2025, 1, 1));

        assertEquals("SINGLE_OR_OTHER", result.familySituation());
        assertEquals("COMUN", result.taxTerritory());
        assertEquals(0, result.descendantsCount());
    }

    @Test
    void returnsDefault_whenNoRecordFound() {
        when(employeeLookupAdapter.findEmployeeId(any(), any(), any())).thenReturn(Optional.of(1L));
        when(springDataRepo.findFirstByEmployeeIdAndValidFromLessThanEqualOrderByValidFromDesc(any(), any()))
            .thenReturn(Optional.empty());

        EmployeeTaxInfoContext result = adapter.findLatestOnOrBefore("ESP", "INTERNAL", "EMP001", LocalDate.of(2025, 1, 1));

        assertEquals("SINGLE_OR_OTHER", result.familySituation());
        assertEquals("COMUN", result.taxTerritory());
        assertEquals(0, result.descendantsCount());
    }

    @Test
    void returnsMappedContext_whenRecordFound() {
        when(employeeLookupAdapter.findEmployeeId(any(), any(), any())).thenReturn(Optional.of(1L));

        EmployeeTaxInformationEntity entity = new EmployeeTaxInformationEntity();
        entity.setId(3L);
        entity.setEmployeeId(1L);
        entity.setValidFrom(LocalDate.of(2025, 1, 1));
        entity.setFamilySituation(FamilySituation.MARRIED_DEPENDENT_SPOUSE);
        entity.setDescendantsCount(2);
        entity.setAscendantsCount(0);
        entity.setDisabilityDegree(DisabilityDegree.NONE);
        entity.setPensionCompensatoria(false);
        entity.setGeographicMobility(true);
        entity.setHabitualResidenceLoan(false);
        entity.setTaxTerritory(TaxTerritory.BIZKAIA);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(springDataRepo.findFirstByEmployeeIdAndValidFromLessThanEqualOrderByValidFromDesc(any(), any()))
            .thenReturn(Optional.of(entity));

        EmployeeTaxInfoContext result = adapter.findLatestOnOrBefore("ESP", "INTERNAL", "EMP001", LocalDate.of(2025, 1, 15));

        assertEquals("MARRIED_DEPENDENT_SPOUSE", result.familySituation());
        assertEquals("BIZKAIA", result.taxTerritory());
        assertTrue(result.geographicMobility());
    }
}
