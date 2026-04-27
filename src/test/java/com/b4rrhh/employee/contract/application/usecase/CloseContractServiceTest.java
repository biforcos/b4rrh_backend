package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.exception.ContractCoverageIncompleteException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseContractServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EmployeeContractLookupPort employeeContractLookupPort;

    private TestContractPresenceCoverageValidator contractPresenceCoverageValidator;
    private CloseContractService service;

    @BeforeEach
    void setUp() {
        contractPresenceCoverageValidator = new TestContractPresenceCoverageValidator();
        service = new CloseContractService(
                contractRepository,
                employeeContractLookupPort,
                contractPresenceCoverageValidator
        );
    }

    @Test
    void closesWhenValid() {
        CloseContractCommand command = new CloseContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
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
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        Contract closed = service.close(command);

        assertEquals(LocalDate.of(2026, 1, 31), closed.getEndDate());

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).update(captor.capture(), any(LocalDate.class));
        assertEquals(LocalDate.of(2026, 1, 31), captor.getValue().getEndDate());
    }

    @Test
    void rejectsCloseWhenAlreadyClosed() {
        CloseContractCommand command = new CloseContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
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

        assertThrows(ContractAlreadyClosedException.class, () -> service.close(command));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void rejectsCloseWhenCoverageWouldHaveGap() {
        CloseContractCommand command = new CloseContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 15)
        );

        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractPresenceCoverageValidator.setIncompleteCoverage(true);
        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(existing));
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        assertThrows(ContractCoverageIncompleteException.class, () -> service.close(command));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
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

    private static final class TestContractPresenceCoverageValidator
            extends ContractPresenceCoverageValidator {

        private boolean incompleteCoverage;

        private TestContractPresenceCoverageValidator() {
            super(null);
        }

        void setIncompleteCoverage(boolean incompleteCoverage) {
            this.incompleteCoverage = incompleteCoverage;
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
            if (incompleteCoverage) {
                throw new ContractCoverageIncompleteException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                );
            }
        }
    }
}
