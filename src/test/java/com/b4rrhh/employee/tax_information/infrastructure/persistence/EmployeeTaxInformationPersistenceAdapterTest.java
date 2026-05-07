package com.b4rrhh.employee.tax_information.infrastructure.persistence;

import com.b4rrhh.employee.tax_information.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeTaxInformationPersistenceAdapterTest {

    @Mock SpringDataEmployeeTaxInformationRepository springDataRepo;
    @InjectMocks EmployeeTaxInformationPersistenceAdapter adapter;

    @Test
    void findLatestOnOrBefore_returnsEmptyWhenNoneExist() {
        when(springDataRepo.findFirstByEmployeeIdAndValidFromLessThanEqualOrderByValidFromDesc(any(), any()))
            .thenReturn(Optional.empty());
        assertTrue(adapter.findLatestOnOrBefore(1L, LocalDate.of(2025,1,1)).isEmpty());
    }

    @Test
    void findLatestOnOrBefore_mapsEntityToDomain() {
        EmployeeTaxInformationEntity entity = new EmployeeTaxInformationEntity();
        entity.setId(5L);
        entity.setEmployeeId(1L);
        entity.setValidFrom(LocalDate.of(2025,1,1));
        entity.setFamilySituation(FamilySituation.SINGLE_OR_OTHER);
        entity.setDescendantsCount(0);
        entity.setAscendantsCount(0);
        entity.setDisabilityDegree(DisabilityDegree.NONE);
        entity.setPensionCompensatoria(false);
        entity.setGeographicMobility(false);
        entity.setHabitualResidenceLoan(false);
        entity.setTaxTerritory(TaxTerritory.COMUN);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(springDataRepo.findFirstByEmployeeIdAndValidFromLessThanEqualOrderByValidFromDesc(any(), any()))
            .thenReturn(Optional.of(entity));

        var result = adapter.findLatestOnOrBefore(1L, LocalDate.of(2025,1,1));
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());
        assertEquals(FamilySituation.SINGLE_OR_OTHER, result.get().getFamilySituation());
    }
}
