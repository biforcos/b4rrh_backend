package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ReplaceContractFromDateCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractSubtypeRelationValidator;
import com.b4rrhh.employee.contract.application.service.ContractCatalogValidator;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
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
class ReplaceContractFromDateServiceTest {

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
    private ReplaceContractFromDateService service;

    @BeforeEach
    void setUp() {
        contractCatalogValidator = new TestContractCatalogValidator();
        contractSubtypeRelationValidator = new TestContractSubtypeRelationValidator();
        contractPresenceCoverageValidator = new TestContractPresenceCoverageValidator();

        service = new ReplaceContractFromDateService(
                contractRepository,
                employeeContractLookupPort,
                contractCatalogValidator,
                contractSubtypeRelationValidator,
                contractPresenceCoverageValidator
        );
    }

    @Test
    void replaceInsideOpenPeriodSplitsTimelineSafely() {
        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        Contract replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "tmp",
                "pt1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(null, replaced.getEndDate());
        assertEquals("TMP", replaced.getContractCode());
        assertEquals("PT1", replaced.getContractSubtypeCode());

        ArgumentCaptor<Contract> updatedCaptor = ArgumentCaptor.forClass(Contract.class);
        ArgumentCaptor<Contract> savedCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).update(updatedCaptor.capture(), any(LocalDate.class));
        verify(contractRepository).save(savedCaptor.capture());

        assertEquals("IND", updatedCaptor.getValue().getContractCode());
        assertEquals("FT1", updatedCaptor.getValue().getContractSubtypeCode());
        assertEquals(LocalDate.of(2026, 1, 1), updatedCaptor.getValue().getStartDate());
        assertEquals(LocalDate.of(2026, 2, 28), updatedCaptor.getValue().getEndDate());

        assertEquals("TMP", savedCaptor.getValue().getContractCode());
        assertEquals("PT1", savedCaptor.getValue().getContractSubtypeCode());
        assertEquals(LocalDate.of(2026, 3, 1), savedCaptor.getValue().getStartDate());
        assertEquals(null, savedCaptor.getValue().getEndDate());

        assertEquals(2, contractPresenceCoverageValidator.lastProjectedHistory.size());
    }

    @Test
    void replaceInsideClosedPeriodPreservesOriginalEndDate() {
        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        Contract replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "TMP",
                "PT1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 31), replaced.getEndDate());

        ArgumentCaptor<Contract> updatedCaptor = ArgumentCaptor.forClass(Contract.class);
        ArgumentCaptor<Contract> savedCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(contractRepository).update(updatedCaptor.capture(), any(LocalDate.class));
        verify(contractRepository).save(savedCaptor.capture());

        assertEquals(LocalDate.of(2026, 2, 28), updatedCaptor.getValue().getEndDate());
        assertEquals(LocalDate.of(2026, 3, 31), savedCaptor.getValue().getEndDate());
    }

    @Test
    void replaceAtExactStartDateUpdatesWithoutDuplicateIdentityRow() {
        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 3, 1),
                null
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        Contract replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "TMP",
                "PT1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals("TMP", replaced.getContractCode());

        verify(contractRepository).update(any(Contract.class), any(LocalDate.class));
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    void rejectsWhenContractSubtypeRelationIsInvalid() {
        contractSubtypeRelationValidator.setInvalidRelation(true);
        whenEmployeeExists();

        assertThrows(
                ContractSubtypeRelationInvalidException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "TMP", "PT1"))
        );
        verify(contractRepository, never()).save(any(Contract.class));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenEmployeeDoesNotExist() {
        when(employeeContractLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.empty());

        assertThrows(
                ContractEmployeeNotFoundException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "TMP", "PT1"))
        );

        verify(contractRepository, never()).save(any(Contract.class));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenReplacementBreaksPresenceCoverage() {
        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractPresenceCoverageValidator.setIncompleteCoverage(true);
        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        assertThrows(
                ContractCoverageIncompleteException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "TMP", "PT1"))
        );

        verify(contractRepository, never()).save(any(Contract.class));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenReplacementIsOutsidePresence() {
        Contract existing = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );

        contractPresenceCoverageValidator.setOutsidePresence(true);
        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        assertThrows(
                ContractOutsidePresencePeriodException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "TMP", "PT1"))
        );

        verify(contractRepository, never()).save(any(Contract.class));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenNoCoveringPeriodAndProjectedTimelineOverlaps() {
        Contract futureOpen = new Contract(
                10L,
                "IND",
                "FT1",
                LocalDate.of(2026, 4, 1),
                null
        );

        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(futureOpen));

        assertThrows(
                ContractOverlapException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "TMP", "PT1"))
        );

        verify(contractRepository, never()).save(any(Contract.class));
        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
    }

    @Test
    void createsNewPeriodWhenNoCoveringAndProjectedTimelineIsValid() {
        whenEmployeeExists();
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        Contract replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "TMP",
                "PT1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(null, replaced.getEndDate());

        verify(contractRepository, never()).update(any(Contract.class), any(LocalDate.class));
        verify(contractRepository).save(any(Contract.class));
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

    private ReplaceContractFromDateCommand command(
            LocalDate effectiveDate,
            String contractCode,
            String contractSubtypeCode
    ) {
        return new ReplaceContractFromDateCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                effectiveDate,
                contractCode,
                contractSubtypeCode
        );
    }

    private static final class TestContractCatalogValidator extends ContractCatalogValidator {

        private final Set<String> invalidContractCodes = new HashSet<>();
        private final Set<String> invalidSubtypeCodes = new HashSet<>();

        private TestContractCatalogValidator() {
            super(null);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException(fieldName + " is required");
            }

            return value.trim().toUpperCase();
        }

        @Override
        public void validateContractCode(String ruleSystemCode, String contractCode, LocalDate referenceDate) {
            if (invalidContractCodes.contains(contractCode)) {
                throw new IllegalArgumentException("contractCode is invalid");
            }
        }

        @Override
        public void validateContractSubtypeCode(
                String ruleSystemCode,
                String contractSubtypeCode,
                LocalDate referenceDate
        ) {
            if (invalidSubtypeCodes.contains(contractSubtypeCode)) {
                throw new IllegalArgumentException("contractSubtypeCode is invalid");
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
        private List<Contract> lastProjectedHistory = List.of();

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
        public void validateFullCoverage(
                Long employeeId,
                List<Contract> projectedContractHistory,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
            lastProjectedHistory = projectedContractHistory;
            if (outsidePresence) {
                Contract sample = projectedContractHistory.isEmpty()
                        ? null
                        : projectedContractHistory.get(0);
                throw new ContractOutsidePresencePeriodException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        sample == null ? null : sample.getStartDate(),
                        sample == null ? null : sample.getEndDate()
                );
            }
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
