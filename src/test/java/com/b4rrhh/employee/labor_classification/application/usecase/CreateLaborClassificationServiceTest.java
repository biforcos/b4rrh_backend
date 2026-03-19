package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.AgreementCategoryRelationValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationCatalogValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.InvalidLaborClassificationDateRangeException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
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
class CreateLaborClassificationServiceTest {

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
    private CreateLaborClassificationService service;

    @BeforeEach
    void setUp() {
        laborClassificationCatalogValidator = new TestLaborClassificationCatalogValidator();
        agreementCategoryRelationValidator = new TestAgreementCategoryRelationValidator();
        laborClassificationPresenceCoverageValidator = new TestLaborClassificationPresenceCoverageValidator();

        service = new CreateLaborClassificationService(
                laborClassificationRepository,
                employeeLaborClassificationLookupPort,
                laborClassificationCatalogValidator,
                agreementCategoryRelationValidator,
                laborClassificationPresenceCoverageValidator
        );
    }

    @Test
    void rejectsWhenEmployeeDoesNotExist() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        when(employeeLaborClassificationLookupPort.findByBusinessKeyForUpdate(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        )).thenReturn(Optional.empty());

        assertThrows(LaborClassificationEmployeeNotFoundException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidAgreementCode() {
        CreateLaborClassificationCommand command = command(
                "BAD_AGR",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationCatalogValidator.markAgreementInvalid("BAD_AGR");
        whenEmployeeExists();

        assertThrows(LaborClassificationAgreementInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidAgreementCategoryCode() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "BAD_CAT",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationCatalogValidator.markCategoryInvalid("BAD_CAT");
        whenEmployeeExists();

        assertThrows(LaborClassificationCategoryInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidAgreementCategoryRelation() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        agreementCategoryRelationValidator.setInvalidRelation(true);
        whenEmployeeExists();

        assertThrows(LaborClassificationAgreementCategoryRelationInvalidException.class, () -> service.create(command));
    }

    @Test
    void rejectsInvalidDateRange() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1)
        );

        whenEmployeeExists();

        assertThrows(InvalidLaborClassificationDateRangeException.class, () -> service.create(command));
    }

    @Test
    void rejectsOverlapOnCreate() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(true);

        assertThrows(LaborClassificationOverlapException.class, () -> service.create(command));
        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
    }

    @Test
    void rejectsOutsidePresenceOnCreate() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationPresenceCoverageValidator.setOutsidePresence(true);
        whenEmployeeExists();
        when(laborClassificationRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(false);

        assertThrows(LaborClassificationOutsidePresencePeriodException.class, () -> service.create(command));
        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
    }

    @Test
    void rejectsIncompleteCoverageOnCreate() {
        CreateLaborClassificationCommand command = command(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationPresenceCoverageValidator.setIncompleteCoverage(true);
        whenEmployeeExists();
        when(laborClassificationRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(false);
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        assertThrows(LaborClassificationCoverageIncompleteException.class, () -> service.create(command));
        verify(laborClassificationRepository, never()).save(any(LaborClassification.class));
    }

    @Test
    void createsWhenValidAndFullCoverage() {
        CreateLaborClassificationCommand command = command(
                "agr_office",
                "cat_admin",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                null
        )).thenReturn(false);
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        LaborClassification created = service.create(command);

        assertEquals("AGR_OFFICE", created.getAgreementCode());
        assertEquals("CAT_ADMIN", created.getAgreementCategoryCode());

        ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository).save(captor.capture());
        assertEquals("AGR_OFFICE", captor.getValue().getAgreementCode());
        assertEquals("CAT_ADMIN", captor.getValue().getAgreementCategoryCode());
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

    private CreateLaborClassificationCommand command(
            String agreementCode,
            String agreementCategoryCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new CreateLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                agreementCode,
                agreementCategoryCode,
                startDate,
                endDate
        );
    }

    private static final class TestLaborClassificationCatalogValidator extends LaborClassificationCatalogValidator {

        private final Set<String> invalidAgreementCodes = new HashSet<>();
        private final Set<String> invalidCategoryCodes = new HashSet<>();

        private TestLaborClassificationCatalogValidator() {
            super(null);
        }

        void markAgreementInvalid(String code) {
            invalidAgreementCodes.add(code);
        }

        void markCategoryInvalid(String code) {
            invalidCategoryCodes.add(code);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                if ("agreementCode".equals(fieldName)) {
                    throw new LaborClassificationAgreementInvalidException(String.valueOf(value));
                }
                throw new LaborClassificationCategoryInvalidException(String.valueOf(value));
            }

            return value.trim().toUpperCase();
        }

        @Override
        public void validateAgreementCode(String ruleSystemCode, String agreementCode, LocalDate referenceDate) {
            if (invalidAgreementCodes.contains(agreementCode)) {
                throw new LaborClassificationAgreementInvalidException(agreementCode);
            }
        }

        @Override
        public void validateAgreementCategoryCode(
                String ruleSystemCode,
                String agreementCategoryCode,
                LocalDate referenceDate
        ) {
            if (invalidCategoryCodes.contains(agreementCategoryCode)) {
                throw new LaborClassificationCategoryInvalidException(agreementCategoryCode);
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
        public void validatePeriodWithinPresence(
                Long employeeId,
                LocalDate startDate,
                LocalDate endDate,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
            if (outsidePresence) {
                throw new LaborClassificationOutsidePresencePeriodException(
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
                List<LaborClassification> projectedLaborClassificationHistory,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
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
