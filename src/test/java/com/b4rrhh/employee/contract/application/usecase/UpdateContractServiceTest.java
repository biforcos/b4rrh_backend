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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
        verify(contractRepository).update(captor.capture());
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
        verify(contractRepository, never()).update(any(Contract.class));
    }

    @Test
    void rejectsUpdateWhenContractSubtypeRelationIsInvalid() {
        UpdateContractCommand command = new UpdateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
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
        verify(contractRepository, never()).update(any(Contract.class));
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
