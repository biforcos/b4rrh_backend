package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCoverageIncompleteException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseLaborClassificationServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private LaborClassificationRepository laborClassificationRepository;
    @Mock
    private EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;

    private TestLaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator;
    private CloseLaborClassificationService service;

    @BeforeEach
    void setUp() {
        laborClassificationPresenceCoverageValidator = new TestLaborClassificationPresenceCoverageValidator();
        service = new CloseLaborClassificationService(
                laborClassificationRepository,
                employeeLaborClassificationLookupPort,
                laborClassificationPresenceCoverageValidator
        );
    }

    @Test
    void closesWhenValid() {
        CloseLaborClassificationCommand command = new CloseLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31)
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
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        LaborClassification closed = service.close(command);

        assertEquals(LocalDate.of(2026, 1, 31), closed.getEndDate());

        ArgumentCaptor<LaborClassification> captor = ArgumentCaptor.forClass(LaborClassification.class);
        verify(laborClassificationRepository).update(captor.capture(), any(LocalDate.class));
        assertEquals(LocalDate.of(2026, 1, 31), captor.getValue().getEndDate());
    }

    @Test
    void rejectsCloseWhenAlreadyClosed() {
        CloseLaborClassificationCommand command = new CloseLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)
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

        assertThrows(LaborClassificationAlreadyClosedException.class, () -> service.close(command));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
    }

    @Test
    void rejectsCloseWhenCoverageWouldHaveGap() {
        CloseLaborClassificationCommand command = new CloseLaborClassificationCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 15)
        );

        LaborClassification existing = new LaborClassification(
                10L,
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );

        laborClassificationPresenceCoverageValidator.setIncompleteCoverage(true);
        whenEmployeeExists();
        when(laborClassificationRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(existing));
        when(laborClassificationRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(existing));

        assertThrows(LaborClassificationCoverageIncompleteException.class, () -> service.close(command));
        verify(laborClassificationRepository, never()).update(any(LaborClassification.class), any(LocalDate.class));
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

    private static final class TestLaborClassificationPresenceCoverageValidator
            extends LaborClassificationPresenceCoverageValidator {

        private boolean incompleteCoverage;

        private TestLaborClassificationPresenceCoverageValidator() {
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
