package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeConflictException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HireEmployeeIdempotencyEvaluatorTest {

    private final HireEmployeeIdempotencyEvaluator evaluator = new HireEmployeeIdempotencyEvaluator();

    @Test
    void returnsEquivalentResultWhenStateIsFunctionallyEquivalent() {
        HireEmployeeIdempotencyEvaluator.HireEmployeeIdempotencyInput input = validInput(LocalDate.of(2026, 3, 23));

        HireEmployeeResult result = evaluator.evaluateOrThrow(input, equivalentCurrentState(LocalDate.of(2026, 3, 23)));

        assertEquals(false, result.created());
        assertEquals("EMP001", result.employeeNumber());
        assertEquals("CON", result.contractTypeCode());
        assertEquals("WC1", result.workCenterCode());
    }

    @Test
    void failsWhenExistingHireDateIsDifferent() {
        HireEmployeeIdempotencyEvaluator.HireEmployeeIdempotencyInput input = validInput(LocalDate.of(2026, 3, 23));

        assertThrows(
                HireEmployeeConflictException.class,
                () -> evaluator.evaluateOrThrow(input, equivalentCurrentState(LocalDate.of(2026, 4, 1)))
        );
    }

    @Test
    void failsWhenExistingContractOrWorkCenterIsDifferent() {
        HireEmployeeIdempotencyEvaluator.HireEmployeeIdempotencyInput input = validInput(LocalDate.of(2026, 3, 23));
        HireEmployeeCurrentState state = new HireEmployeeCurrentState(
                activeEmployee(),
                List.of(activePresence(LocalDate.of(2026, 3, 23))),
                List.of(activeLaborClassification(LocalDate.of(2026, 3, 23))),
                List.of(new Contract(1L, "TMP", "SUB", LocalDate.of(2026, 3, 23), null)),
                List.of(new WorkCenter(10L, 1L, 1, "WC9", LocalDate.of(2026, 3, 23), null, LocalDateTime.now(), LocalDateTime.now()))
        );

        assertThrows(HireEmployeeConflictException.class, () -> evaluator.evaluateOrThrow(input, state));
    }

    @Test
    void failsWhenExistingStateIsPartialOrInconsistent() {
        HireEmployeeIdempotencyEvaluator.HireEmployeeIdempotencyInput input = validInput(LocalDate.of(2026, 3, 23));
        HireEmployeeCurrentState state = new HireEmployeeCurrentState(
                activeEmployee(),
                List.of(activePresence(LocalDate.of(2026, 3, 23))),
                List.of(activeLaborClassification(LocalDate.of(2026, 3, 23))),
                List.of(),
                List.of(activeWorkCenter(LocalDate.of(2026, 3, 23)))
        );

        assertThrows(HireEmployeeConflictException.class, () -> evaluator.evaluateOrThrow(input, state));
    }

    private HireEmployeeIdempotencyEvaluator.HireEmployeeIdempotencyInput validInput(LocalDate hireDate) {
        return new HireEmployeeIdempotencyEvaluator.HireEmployeeIdempotencyInput(
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                hireDate,
                "COMP",
                "HIRE",
                "AGR",
                "CAT",
                "CON",
                "SUB",
                "WC1"
        );
    }

    private HireEmployeeCurrentState equivalentCurrentState(LocalDate hireDate) {
        return new HireEmployeeCurrentState(
                activeEmployee(),
                List.of(activePresence(hireDate)),
                List.of(activeLaborClassification(hireDate)),
                List.of(activeContract(hireDate)),
                List.of(activeWorkCenter(hireDate))
        );
    }

    private Employee activeEmployee() {
        return new Employee(
                1L,
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Presence activePresence(LocalDate hireDate) {
        return new Presence(
                10L,
                1L,
                1,
                "COMP",
                "HIRE",
                null,
                hireDate,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private LaborClassification activeLaborClassification(LocalDate hireDate) {
        return new LaborClassification(
                1L,
                "AGR",
                "CAT",
                hireDate,
                null
        );
    }

    private Contract activeContract(LocalDate hireDate) {
        return new Contract(
                1L,
                "CON",
                "SUB",
                hireDate,
                null
        );
    }

    private WorkCenter activeWorkCenter(LocalDate hireDate) {
        return new WorkCenter(
                10L,
                1L,
                1,
                "WC1",
                hireDate,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
