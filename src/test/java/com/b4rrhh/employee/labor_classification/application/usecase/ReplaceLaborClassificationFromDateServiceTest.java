package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ReplaceLaborClassificationFromDateCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.AgreementCategoryRelationValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationCatalogValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCoverageIncompleteException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOutsidePresencePeriodException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOverlapException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.domain.port.LaborClassificationRepository;
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
class ReplaceLaborClassificationFromDateServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private LaborClassificationRepository laborClassificationRepository;
    @Mock
    private EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;

    private TestLaborClassificationCatalogValidator laborClassificationCatalogValidator;
    private TestAgreementCategoryRelationValidator agreementCategoryRelationValidator;
    private TestLaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator;
    private ReplaceLaborClassificationFromDateService service;

    @BeforeEach
    void setUp() {
        laborClassificationCatalogValidator = new TestLaborClassificationCatalogValidator();
        agreementCategoryRelationValidator = new TestAgreementCategoryRelationValidator();
        laborClassificationPresenceCoverageValidator = new TestLaborClassificationPresenceCoverageValidator();

        service = new ReplaceLaborClassificationFromDateService(
                laborClassificationRepository,
                employeeLaborClassificationLookupPort,
                laborClassificationCatalogValidator,
                agreementCategoryRelationValidator,
                laborClassificationPresenceCoverageValidator
        );
    }

    @Test
    void replaceInsideOpenPeriodSplitsTimelineSafely() {
        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        LaborClassification replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "agr_tech",
                "cat_tech_1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(null, replaced.getEndDate());
        assertEquals("AGR_TECH", replaced.getAgreementCode());
        assertEquals("CAT_TECH_1", replaced.getAgreementCategoryCode());

        ArgumentCaptor<LaborClassification> updatedCaptor = ArgumentCaptor.forClass(LaborClassification.class);
        ArgumentCaptor<LaborClassification> savedCaptor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository).update(updatedCaptor.capture(), any(LocalDate.class));
        verify(laborClassificationRepository).save(savedCaptor.capture());

        assertEquals("AGR_OFFICE", updatedCaptor.getValue().getAgreementCode());
        assertEquals("CAT_ADMIN", updatedCaptor.getValue().getAgreementCategoryCode());
        assertEquals(LocalDate.of(2026, 1, 1), updatedCaptor.getValue().getStartDate());
        assertEquals(LocalDate.of(2026, 2, 28), updatedCaptor.getValue().getEndDate());

        assertEquals("AGR_TECH", savedCaptor.getValue().getAgreementCode());
        assertEquals("CAT_TECH_1", savedCaptor.getValue().getAgreementCategoryCode());
        assertEquals(LocalDate.of(2026, 3, 1), savedCaptor.getValue().getStartDate());
        assertEquals(null, savedCaptor.getValue().getEndDate());

        assertEquals(2, laborClassificationPresenceCoverageValidator.lastProjectedHistory.size());
    }

    @Test
    void replaceInsideClosedPeriodPreservesOriginalEndDate() {
        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        LaborClassification replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "AGR_TECH",
                "CAT_TECH_1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(LocalDate.of(2026, 3, 31), replaced.getEndDate());

        ArgumentCaptor<LaborClassification> updatedCaptor = ArgumentCaptor.forClass(LaborClassification.class);
        ArgumentCaptor<LaborClassification> savedCaptor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository).update(updatedCaptor.capture(), any(LocalDate.class));
        verify(laborClassificationRepository).save(savedCaptor.capture());

        assertEquals(LocalDate.of(2026, 2, 28), updatedCaptor.getValue().getEndDate());
        assertEquals(LocalDate.of(2026, 3, 31), savedCaptor.getValue().getEndDate());
    }

    @Test
    void replaceAtExactStartDateUpdatesWithoutDuplicateIdentityRow() {
        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 3, 1),
                null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        LaborClassification replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "AGR_TECH",
                "CAT_TECH_1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals("AGR_TECH", replaced.getAgreementCode());

        verify(laborClassificationRepository).update(any(LaborClassification.class), any(LocalDate.class));
        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
    }

    @Test
    void rejectsWhenAgreementCategoryRelationIsInvalid() {
        agreementCategoryRelationValidator.setInvalidRelation(true);
        whenEmployeeExists();

        assertThrows(
                LaborClassificationAgreementCategoryRelationInvalidException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "AGR_TECH", "CAT_TECH_1"))
        );
        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenEmployeeDoesNotExist() {
        when(employeeLaborClassificationLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.empty());

        assertThrows(
                LaborClassificationEmployeeNotFoundException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "AGR_TECH", "CAT_TECH_1"))
        );

        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenReplacementBreaksPresenceCoverage() {
        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationPresenceCoverageValidator.setIncompleteCoverage(true);
        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        assertThrows(
                LaborClassificationCoverageIncompleteException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "AGR_TECH", "CAT_TECH_1"))
        );

        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenReplacementIsOutsidePresence() {
        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationPresenceCoverageValidator.setOutsidePresence(true);
        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        assertThrows(
                LaborClassificationOutsidePresencePeriodException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "AGR_TECH", "CAT_TECH_1"))
        );

        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void rejectsWhenNoCoveringPeriodAndProjectedTimelineOverlaps() {
        LaborClassification futureOpen = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 4, 1),
                null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(futureOpen));

        assertThrows(
                LaborClassificationOverlapException.class,
                () -> service.replaceFromDate(command(LocalDate.of(2026, 3, 1), "AGR_TECH", "CAT_TECH_1"))
        );

        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void createsNewPeriodWhenNoCoveringAndProjectedTimelineIsValid() {
        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        LaborClassification replaced = service.replaceFromDate(command(
                LocalDate.of(2026, 3, 1),
                "AGR_TECH",
                "CAT_TECH_1"
        ));

        assertEquals(LocalDate.of(2026, 3, 1), replaced.getStartDate());
        assertEquals(null, replaced.getEndDate());

        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
        verify(laborClassificationRepository).save(any(LaborClassification.class));
    }

    private void whenEmployeeExists() {
        when(employeeLaborClassificationLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.of(new EmployeeLaborClassificationContext(
                10L,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )));
    }

    private ReplaceLaborClassificationFromDateCommand command(
            LocalDate effectiveDate,
            String agreementCode,
            String agreementCategoryCode
    ) {
        return new ReplaceLaborClassificationFromDateCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                effectiveDate,
                agreementCode,
                agreementCategoryCode
        );
    }

    private static final class TestLaborClassificationCatalogValidator extends LaborClassificationCatalogValidator {

        private final Set<String> invalidAgreementCodes = new HashSet<>();
        private final Set<String> invalidCategoryCodes = new HashSet<>();

        private TestLaborClassificationCatalogValidator() {
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
        public void validateAgreementCode(String ruleSystemCode, String agreementCode, LocalDate referenceDate) {
            if (invalidAgreementCodes.contains(agreementCode)) {
                throw new IllegalArgumentException("agreementCode is invalid");
            }
        }

        @Override
        public void validateAgreementCategoryCode(
                String ruleSystemCode,
                String agreementCategoryCode,
                LocalDate referenceDate
        ) {
            if (invalidCategoryCodes.contains(agreementCategoryCode)) {
                throw new IllegalArgumentException("agreementCategoryCode is invalid");
            }
        }
    }

    private static final class TestAgreementCategoryRelationValidator extends AgreementCategoryRelationValidator {

        private boolean invalidRelation;

        private TestAgreementCategoryRelationValidator() {
            super(null);
        }

        void setInvalidRelation(boolean invalidRelation) {
            this.invalidRelation = invalidRelation;
        }

        @Override
        public void validateAgreementCategoryRelation(
                String ruleSystemCode,
                String agreementCode,
                String agreementCategoryCode,
                LocalDate referenceDate
        ) {
            if (invalidRelation) {
                throw new LaborClassificationAgreementCategoryRelationInvalidException(
                        ruleSystemCode,
                        agreementCode,
                        agreementCategoryCode,
                        referenceDate
                );
            }
        }
    }

    private static final class TestLaborClassificationPresenceCoverageValidator
            extends LaborClassificationPresenceCoverageValidator {

        private boolean outsidePresence;
        private boolean incompleteCoverage;
        private List<LaborClassification> lastProjectedHistory = List.of();

        private TestLaborClassificationPresenceCoverageValidator() {
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
                List<LaborClassification> projectedLaborClassificationHistory,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
            lastProjectedHistory = projectedLaborClassificationHistory;
            if (outsidePresence) {
                LaborClassification sample = projectedLaborClassificationHistory.isEmpty()
                        ? null
                        : projectedLaborClassificationHistory.get(0);
                throw new LaborClassificationOutsidePresencePeriodException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        sample == null ? null : sample.getStartDate(),
                        sample == null ? null : sample.getEndDate()
                );
            }
            if (incompleteCoverage) {
                throw new LaborClassificationCoverageIncompleteException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                );
            }
        }
    }
}
