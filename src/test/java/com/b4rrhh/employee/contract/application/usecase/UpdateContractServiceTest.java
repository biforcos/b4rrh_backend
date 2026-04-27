package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.UpdateContractCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractSubtypeRelationValidator;
import com.b4rrhh.employee.contract.application.service.ContractCatalogValidator;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateContractServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EmployeeContractLookupPort employeeContractLookupPort;

    private TestContractCatalogValidator contractCatalogValidator;
    private TestContractSubtypeRelationValidator contractSubtypeRelationValidator;
    private TestContractPresenceCoverageValidator contractPresenceCoverageValidator;
    private UpdateContractService service;

    @BeforeEach
    void setUp() {
        contractCatalogValidator = new TestContractCatalogValidator();
        contractSubtypeRelationValidator = new TestContractSubtypeRelationValidator();
        contractPresenceCoverageValidator = new TestContractPresenceCoverageValidator();

        service = new UpdateContractService(
                contractRepository,
                employeeContractLookupPort,
                contractCatalogValidator,
                contractSubtypeRelationValidator,
                contractPresenceCoverageValidator
        );
    }

    @Test
    void updatesWhenValid() {
        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                null,
                "TMP",
                "PT1"
        );

        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(existing));
        when(contractRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                LocalDate.of(2026, 1, 1)
        )).thenReturn(false);
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        Contract updated = service.update(command);

        assertEquals("TMP", updated.getContractCode());
        assertEquals("PT1", updated.getContractSubtypeCode());

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).update(captor.capture(), any(LocalDate.class));
        assertEquals("TMP", captor.getValue().getContractCode());
        assertEquals("PT1", captor.getValue().getContractSubtypeCode());
    }

    @Test
    void rejectsUpdateWhenRecordIsClosed() {
        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                null,
                "TMP",
                "PT1"
        );

        Contract closed = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(closed));

        assertThrows(ContractAlreadyClosedException.class, () -> service.update(command));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void rejectsUpdateWhenContractSubtypeRelationIsInvalid() {
        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                null,
                "TMP",
                "PT1"
        );

        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractSubtypeRelationValidator.setInvalidRelation(true);
        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(existing));

        assertThrows(
                ContractSubtypeRelationInvalidException.class,
                () -> service.update(command)
        );
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void whenNewStartDateDiffers_predecessorEndDateIsCascaded() {
        LocalDate predecessorStart = LocalDate.of(2024, 1, 1);
        LocalDate predecessorEnd   = LocalDate.of(2024, 12, 31);
        LocalDate currentStart     = LocalDate.of(2025, 1, 1);
        LocalDate newStart         = LocalDate.of(2025, 2, 1);

        Contract predecessor = Contract.rehydrate(10L, "IND", "FT1", predecessorStart, predecessorEnd);
        Contract current     = Contract.rehydrate(10L, "IND", "FT1", currentStart, null);

        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                currentStart,
                newStart,
                "IND",
                "FT1"
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, currentStart))
                .thenReturn(Optional.of(current));
        when(contractRepository.existsOverlappingPeriod(any(), any(), any(), any()))
                .thenReturn(false);
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(predecessor, current));

        service.update(command);

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository, times(2)).update(captor.capture(), any(LocalDate.class));

        List<Contract> saved = captor.getAllValues();
        Contract savedPredecessor = saved.get(0);
        Contract savedCurrent     = saved.get(1);

        assertEquals(predecessorStart,               savedPredecessor.getStartDate());
        assertEquals(newStart.minusDays(1),          savedPredecessor.getEndDate());
        assertEquals(newStart,                       savedCurrent.getStartDate());
    }

    @Test
    void whenNewStartDateDiffers_andNoPredecessor_onlyCurrentIsUpdated() {
        LocalDate currentStart = LocalDate.of(2025, 1, 1);
        LocalDate newStart     = LocalDate.of(2025, 2, 1);

        Contract current = Contract.rehydrate(10L, "IND", "FT1", currentStart, null);

        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                currentStart,
                newStart,
                "IND",
                "FT1"
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, currentStart))
                .thenReturn(Optional.of(current));
        when(contractRepository.existsOverlappingPeriod(any(), any(), any(), any()))
                .thenReturn(false);
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(current));

        service.update(command);

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).update(captor.capture(), any(LocalDate.class));
        assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    void whenNewStartDateIsEarlier_predecessorEndDateIsCascadedBackward() {
        // predecessor ends just before current starts (2025-01-01)
        Contract predecessor = Contract.rehydrate(
                10L, "CTR", "ORD",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        Contract current = Contract.rehydrate(
                10L, "CTR", "ORD",
                LocalDate.of(2025, 1, 1), null
        );
        // Move current's startDate EARLIER to 2024-12-15
        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2025, 1, 1)))
                .thenReturn(Optional.of(current));
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(predecessor, current));
        when(contractRepository.existsOverlappingPeriod(any(), any(), any(), any()))
                .thenReturn(false);

        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                LocalDate.of(2025, 1, 1),    // path key
                LocalDate.of(2024, 12, 15),  // newStartDate — earlier
                "CTR", "ORD"
        );

        service.update(command);

        // Predecessor's endDate should be cascaded to newStartDate - 1 = 2024-12-14
        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository, times(2)).update(captor.capture(), any(LocalDate.class));
        List<Contract> saved = captor.getAllValues();
        Contract savedPredecessor = saved.stream()
                .filter(c -> c.getStartDate().equals(LocalDate.of(2024, 1, 1)))
                .findFirst().orElseThrow();
        Contract savedCurrent = saved.stream()
                .filter(c -> c.getStartDate().equals(LocalDate.of(2024, 12, 15)))
                .findFirst().orElseThrow();
        assertThat(savedPredecessor.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 14));
        assertThat(savedCurrent.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 15));
    }

    private void whenEmployeeExists() {
        when(employeeContractLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.of(new EmployeeContractContext(
                10L,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )));
    }

    private static final class TestContractCatalogValidator extends ContractCatalogValidator {

        private TestContractCatalogValidator() {
            super(null);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            return value == null ? null : value.trim().toUpperCase();
        }

        @Override
        public void validateContractCode(String ruleSystemCode, String contractCode, LocalDate referenceDate) {
            // Always valid in these tests.
        }

        @Override
        public void validateContractSubtypeCode(
                String ruleSystemCode,
                String contractSubtypeCode,
                LocalDate referenceDate
        ) {
            // Always valid in these tests.
        }
    }

    private static final class TestContractSubtypeRelationValidator extends ContractSubtypeRelationValidator {

        private boolean invalidRelation;

        private TestContractSubtypeRelationValidator() {
            super(null);
        }

        void setInvalidRelation(boolean invalidRelation) {
            this.invalidRelation = invalidRelation;
        }

        @Override
        public void validateContractSubtypeRelation(
                String ruleSystemCode,
                String contractCode,
                String contractSubtypeCode,
                LocalDate referenceDate
        ) {
            if (invalidRelation) {
                throw new ContractSubtypeRelationInvalidException(
                        ruleSystemCode,
                        contractCode,
                        contractSubtypeCode,
                        referenceDate
                );
            }
        }
    }

    private static final class TestContractPresenceCoverageValidator
            extends ContractPresenceCoverageValidator {

        private TestContractPresenceCoverageValidator() {
            super(null);
        }

        @Override
        public void validatePeriodWithinPresence(
                Long employeeId,
                LocalDate startDate,
                LocalDate endDate,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
            // Always valid in these tests.
        }

        @Override
        public void validateFullCoverage(
                Long employeeId,
                List<Contract> projectedContractHistory,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
            // Always valid in these tests.
        }
    }
}
