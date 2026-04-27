package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.UpdateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.AgreementCategoryRelationValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationCatalogValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.domain.port.LaborClassificationRepository;
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
class UpdateLaborClassificationServiceTest {

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
    private UpdateLaborClassificationService service;

    @BeforeEach
    void setUp() {
        laborClassificationCatalogValidator = new TestLaborClassificationCatalogValidator();
        agreementCategoryRelationValidator = new TestAgreementCategoryRelationValidator();
        laborClassificationPresenceCoverageValidator = new TestLaborClassificationPresenceCoverageValidator();

        service = new UpdateLaborClassificationService(
                laborClassificationRepository,
                employeeLaborClassificationLookupPort,
                laborClassificationCatalogValidator,
                agreementCategoryRelationValidator,
                laborClassificationPresenceCoverageValidator
        );
    }

    @Test
    void updatesWhenValid() {
        UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                null,
                "AGR_TECH",
                "CAT_TECH_1"
        );

        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(existing));
        when(laborClassificationRepository.existsOverlappingPeriod(
                10L,
                LocalDate.of(2026, 1, 1),
                null,
                LocalDate.of(2026, 1, 1)
        )).thenReturn(false);
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        LaborClassification updated = service.update(command);

        assertEquals("AGR_TECH", updated.getAgreementCode());
        assertEquals("CAT_TECH_1", updated.getAgreementCategoryCode());

        ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository).update(captor.capture(), any(LocalDate.class));
        assertEquals("AGR_TECH", captor.getValue().getAgreementCode());
        assertEquals("CAT_TECH_1", captor.getValue().getAgreementCategoryCode());
    }

    @Test
    void rejectsUpdateWhenRecordIsClosed() {
        UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                null,
                "AGR_TECH",
                "CAT_TECH_1"
        );

        LaborClassification closed = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(closed));

        assertThrows(LaborClassificationAlreadyClosedException.class, () -> service.update(command));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void rejectsUpdateWhenAgreementCategoryRelationIsInvalid() {
        UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                null,
                "AGR_TECH",
                "CAT_TECH_1"
        );

        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        agreementCategoryRelationValidator.setInvalidRelation(true);
        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(existing));

        assertThrows(
                LaborClassificationAgreementCategoryRelationInvalidException.class,
                () -> service.update(command)
        );
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void whenNewStartDateDiffers_predecessorEndDateIsCascaded() {
        LocalDate predecessorStart = LocalDate.of(2024, 1, 1);
        LocalDate predecessorEnd   = LocalDate.of(2024, 12, 31);
        LocalDate currentStart     = LocalDate.of(2025, 1, 1);
        LocalDate newStart         = LocalDate.of(2025, 2, 1);

        LaborClassification predecessor = new LaborClassification(
                10L, "AGR_TECH", "CAT_TECH_1", predecessorStart, predecessorEnd
        );
        LaborClassification current = new LaborClassification(
                10L, "AGR_TECH", "CAT_TECH_1", currentStart, null
        );

        UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                currentStart,
                newStart,
                "AGR_TECH",
                "CAT_TECH_1"
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, currentStart))
                .thenReturn(Optional.of(current));
        when(laborClassificationRepository.existsOverlappingPeriod(any(), any(), any(), any()))
                .thenReturn(false);
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(predecessor, current));

        service.update(command);

        ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository, times(2)).update(captor.capture(), any(LocalDate.class));

        List<LaborClassification> saved = captor.getAllValues();
        LaborClassification savedPredecessor = saved.get(0);
        LaborClassification savedCurrent     = saved.get(1);

        assertEquals(predecessorStart,               savedPredecessor.getStartDate());
        assertEquals(newStart.minusDays(1),          savedPredecessor.getEndDate());
        assertEquals(newStart,                       savedCurrent.getStartDate());
    }

    @Test
    void whenNewStartDateDiffers_andNoPredecessor_onlyCurrentIsUpdated() {
        LocalDate currentStart = LocalDate.of(2025, 1, 1);
        LocalDate newStart     = LocalDate.of(2025, 2, 1);

        LaborClassification current = new LaborClassification(
                10L, "AGR_TECH", "CAT_TECH_1", currentStart, null
        );

        UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                currentStart,
                newStart,
                "AGR_TECH",
                "CAT_TECH_1"
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, currentStart))
                .thenReturn(Optional.of(current));
        when(laborClassificationRepository.existsOverlappingPeriod(any(), any(), any(), any()))
                .thenReturn(false);
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(current));

        service.update(command);

        ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository).update(captor.capture(), any(LocalDate.class));
        assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
    }

    @Test
    void whenNewStartDateIsEarlier_predecessorEndDateIsCascadedBackward() {
        LaborClassification predecessor = new LaborClassification(
                10L, "AGR_TECH", "CAT_TECH_1",
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );
        LaborClassification current = new LaborClassification(
                10L, "AGR_TECH", "CAT_TECH_1",
                LocalDate.of(2025, 1, 1), null
        );

        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2025, 1, 1)))
                .thenReturn(Optional.of(current));
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(predecessor, current));
        when(laborClassificationRepository.existsOverlappingPeriod(any(), any(), any(), any()))
                .thenReturn(false);

        UpdateLaborClassificationCommand command = new UpdateLaborClassificationCommand(
                RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER,
                LocalDate.of(2025, 1, 1),    // path key
                LocalDate.of(2024, 12, 15),  // newStartDate — earlier
                "AGR_TECH", "CAT_TECH_1"
        );

        service.update(command);

        ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository, times(2)).update(captor.capture(), any(LocalDate.class));
        List<LaborClassification> saved = captor.getAllValues();
        LaborClassification savedPredecessor = saved.stream()
                .filter(c -> c.getStartDate().equals(LocalDate.of(2024, 1, 1)))
                .findFirst().orElseThrow();
        LaborClassification savedCurrent = saved.stream()
                .filter(c -> c.getStartDate().equals(LocalDate.of(2024, 12, 15)))
                .findFirst().orElseThrow();
        assertThat(savedPredecessor.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 14));
        assertThat(savedCurrent.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 15));
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

    private static final class TestLaborClassificationCatalogValidator extends LaborClassificationCatalogValidator {

        private TestLaborClassificationCatalogValidator() {
            super(null);
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            return value == null ? null : value.trim().toUpperCase();
        }

        @Override
        public void validateAgreementCode(String ruleSystemCode, String agreementCode, LocalDate referenceDate) {
            // Always valid in these tests.
        }

        @Override
        public void validateAgreementCategoryCode(
                String ruleSystemCode,
                String agreementCategoryCode,
                LocalDate referenceDate
        ) {
            // Always valid in these tests.
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

        private TestLaborClassificationPresenceCoverageValidator() {
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
                List<LaborClassification> projectedLaborClassificationHistory,
                String ruleSystemCode,
                String employeeTypeCode,
                String employeeNumber
        ) {
            // Always valid in these tests.
        }
    }
}
