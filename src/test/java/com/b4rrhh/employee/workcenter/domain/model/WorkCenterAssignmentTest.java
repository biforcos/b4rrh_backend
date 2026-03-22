package com.b4rrhh.employee.workcenter.domain.model;

import com.b4rrhh.employee.workcenter.domain.exception.InvalidWorkCenterDateRangeException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterAlreadyClosedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkCenterAssignmentTest {

    @Test
    void closeReturnsClosedAssignmentWhenActive() {
        WorkCenterAssignment assignment = assignment("MADRID_HQ", LocalDate.of(2026, 1, 10), null);

        WorkCenterAssignment closed = assignment.close(LocalDate.of(2026, 1, 20));

        assertEquals(LocalDate.of(2026, 1, 20), closed.getEndDate());
        assertEquals(assignment.getWorkCenterAssignmentNumber(), closed.getWorkCenterAssignmentNumber());
    }

    @Test
    void closeRejectsAlreadyClosedAssignment() {
        WorkCenterAssignment assignment = assignment(
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20)
        );

        assertThrows(WorkCenterAlreadyClosedException.class, () -> assignment.close(LocalDate.of(2026, 1, 21)));
    }

    @Test
    void correctKeepsFunctionalIdentity() {
        WorkCenterAssignment assignment = assignment("MADRID_HQ", LocalDate.of(2026, 1, 10), null);

        WorkCenterAssignment corrected = assignment.correct(
                "BARCELONA_HQ",
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 20)
        );

        assertEquals(assignment.getId(), corrected.getId());
        assertEquals(assignment.getEmployeeId(), corrected.getEmployeeId());
        assertEquals(assignment.getWorkCenterAssignmentNumber(), corrected.getWorkCenterAssignmentNumber());
        assertEquals("BARCELONA_HQ", corrected.getWorkCenterCode());
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(
                InvalidWorkCenterDateRangeException.class,
                () -> assignment("MADRID_HQ", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31))
        );
    }

    private WorkCenterAssignment assignment(String code, LocalDate startDate, LocalDate endDate) {
        return new WorkCenterAssignment(
                1L,
                10L,
                1,
                code,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
