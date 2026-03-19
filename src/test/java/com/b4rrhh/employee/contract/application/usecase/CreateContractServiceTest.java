package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractSubtypeRelationValidator;
import com.b4rrhh.employee.contract.application.service.ContractCatalogValidator;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.InvalidContractDateRangeException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractCoverageIncompleteException;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractOutsidePresencePeriodException;
import com.b4rrhh.employee.contract.domain.exception.ContractOverlapException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateContractServiceTest {

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
    private CreateContractService service;

    @BeforeEach
    void setUp() {
        contractCatalogValidator = new TestContractCatalogValidator();
        contractSubtypeRelationValidator = new TestContractSubtypeRelationValidator();
        contractPresenceCoverageValidator = new TestContractPresenceCoverageValidator();

        service = new CreateContractService(
                contractRepository,
                employeeContractLookupPort,
                contractCatalogValidator,
                contractSubtypeRelationValidator,
                contractPresenceCoverageValidator
        );
    }

    @Test
    void rejectsWhenEmployeeDoesNotExist() {
        CreateContractCommand command = command(
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        when(employeeContractLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.empty());

        assertThrows(ContractEmployeeNotFoundException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidContractCode() {
        CreateContractCommand command = command(
            "BAD",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractCatalogValidator.markContractInvalid("BAD");
        whenEmployeeExists();

        assertThrows(ContractInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidContractSubtypeCode() {
        CreateContractCommand command = command(
                "IND",
            "BCT",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractCatalogValidator.markSubtypeInvalid("BCT");
        whenEmployeeExists();

        assertThrows(ContractSubtypeInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidContractSubtypeRelation() {
        CreateContractCommand command = command(
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractSubtypeRelationValidator.setInvalidRelation(true);
        whenEmployeeExists();

        assertThrows(ContractSubtypeRelationInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidDateRange() {
        CreateContractCommand command = command(
                "IND",
                "FT1",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1)
        );

        whenEmployeeExists();

        assertThrows(InvalidContractDateRangeException.class, () -> service.create(command));
    }

    @Test
    void rejectsOverlapOnCreate() {
        CreateContractCommand command = command(
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(contractRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(true);

        assertThrows(ContractOverlapException.class, () -> service.create(command));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void rejectsOutsidePresenceOnCreate() {
        CreateContractCommand command = command(
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractPresenceCoverageValidator.setOutsidePresence(true);
        whenEmployeeExists();
        when(contractRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(false);

        assertThrows(ContractOutsidePresencePeriodException.class, () -> service.create(command));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void rejectsIncompleteCoverageOnCreate() {
        CreateContractCommand command = command(
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractPresenceCoverageValidator.setIncompleteCoverage(true);
        whenEmployeeExists();
        when(contractRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(false);
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        assertThrows(ContractCoverageIncompleteException.class, () -> service.create(command));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void createsWhenValidAndFullCoverage() {
        CreateContractCommand command = command(
                "ind",
                "ft1",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(contractRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(false);
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        Contract created = service.create(command);

        assertEquals("IND", created.getContractCode());
        assertEquals("FT1", created.getContractSubtypeCode());

        ArgumentCaptor<Contract> captor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).save(captor.capture());
        assertEquals("IND", captor.getValue().getContractCode());
        assertEquals("FT1", captor.getValue().getContractSubtypeCode());
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

    private CreateContractCommand command(
            String contractCode,
            String contractSubtypeCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new CreateContractCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                contractCode,
                contractSubtypeCode,
                startDate,
                endDate
        );
    }

    private static final class TestContractCatalogValidator extends ContractCatalogValidator {

        private final Set<String> invalidContractCodes = new HashSet<>();
        private final Set<String> invalidSubtypeCodes = new HashSet<>();

        private TestContractCatalogValidator() {
            super(null);
        }

        void markContractInvalid(String code) {
            invalidContractCodes.add(code);
        }

        void markSubtypeInvalid(String code) {
            invalidSubtypeCodes.add(code);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                if ("contractCode".equals(fieldName)) {
                    throw new ContractInvalidException(String.valueOf(value));
                }
                throw new ContractSubtypeInvalidException(String.valueOf(value));
            }

            return value.trim().toUpperCase();
        }

        @Override
        public void validateContractCode(String ruleSystemCode, String contractCode, LocalDate referenceDate) {
            if (invalidContractCodes.contains(contractCode)) {
                throw new ContractInvalidException(contractCode);
            }
        }

        @Override
        public void validateContractSubtypeCode(
                String ruleSystemCode,
                String contractSubtypeCode,
                LocalDate referenceDate
        ) {
            if (invalidSubtypeCodes.contains(contractSubtypeCode)) {
                throw new ContractSubtypeInvalidException(contractSubtypeCode);
            }
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

        private boolean outsidePresence;
        private boolean incompleteCoverage;

        private TestContractPresenceCoverageValidator() {
            super(null);
        }

        void setOutsidePresence(boolean outsidePresence) {
            this.outsidePresence = outsidePresence;
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
            if (outsidePresence) {
                throw new ContractOutsidePresencePeriodException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        startDate,
                        endDate
                );
            }
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
