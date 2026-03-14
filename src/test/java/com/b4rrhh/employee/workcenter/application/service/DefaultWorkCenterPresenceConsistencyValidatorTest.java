package com.b4rrhh.employee.workcenter.application.service;

import com.b4rrhh.employee.workcenter.application.port.PresencePeriod;
import com.b4rrhh.employee.workcenter.application.port.WorkCenterPresenceConsistencyPort;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterPresenceCoverageGapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultWorkCenterPresenceConsistencyValidatorTest {

    @Mock
    private WorkCenterPresenceConsistencyPort workCenterPresenceConsistencyPort;

    @Test
    void acceptsPeriodWhenContainedInPresence() {
        DefaultWorkCenterPresenceConsistencyValidator validator =
                new DefaultWorkCenterPresenceConsistencyValidator(workCenterPresenceConsistencyPort, false);

        when(workCenterPresenceConsistencyPort.existsPresenceContainingPeriod(
                        10L, LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 20)))
                .thenReturn(true);

        assertDoesNotThrow(() -> validator.validatePeriodWithinPresence(
                10L,
                LocalDate.of(2026, 1, 5),
                LocalDate.of(2026, 1, 20),
                "ESP",
                "INTERNAL",
                "EMP001"
        ));
    }

    @Test
    void rejectsPeriodWhenNoContainingPresenceExists() {
        DefaultWorkCenterPresenceConsistencyValidator validator =
                new DefaultWorkCenterPresenceConsistencyValidator(workCenterPresenceConsistencyPort, false);

        when(workCenterPresenceConsistencyPort.existsPresenceContainingPeriod(10L, LocalDate.of(2026, 1, 1), null))
                .thenReturn(false);

        assertThrows(
                WorkCenterOutsidePresencePeriodException.class,
                () -> validator.validatePeriodWithinPresence(
                        10L,
                        LocalDate.of(2026, 1, 1),
                        null,
                        "ESP",
                        "INTERNAL",
                        "EMP001"
                )
        );
    }

    @Test
    void skipsCoverageValidationWhenCoverageRuleIsDisabled() {
        DefaultWorkCenterPresenceConsistencyValidator validator =
                new DefaultWorkCenterPresenceConsistencyValidator(workCenterPresenceConsistencyPort, false);

        validator.validatePresenceCoverageIfRequired(
                10L,
                List.of(workCenter(1, LocalDate.of(2026, 1, 1), null)),
                "ESP",
                "INTERNAL",
                "EMP001"
        );

        verifyNoInteractions(workCenterPresenceConsistencyPort);
    }

    @Test
    void validatesCoverageWhenRequiredAndNoGapsExist() {
        DefaultWorkCenterPresenceConsistencyValidator validator =
                new DefaultWorkCenterPresenceConsistencyValidator(workCenterPresenceConsistencyPort, true);

        when(workCenterPresenceConsistencyPort.findPresencePeriodsByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(new PresencePeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10))));

        assertDoesNotThrow(() -> validator.validatePresenceCoverageIfRequired(
                10L,
                List.of(
                        workCenter(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)),
                        workCenter(2, LocalDate.of(2026, 1, 6), LocalDate.of(2026, 1, 10))
                ),
                "ESP",
                "INTERNAL",
                "EMP001"
        ));
    }

    @Test
    void rejectsCoverageWhenRequiredAndGapExists() {
        DefaultWorkCenterPresenceConsistencyValidator validator =
                new DefaultWorkCenterPresenceConsistencyValidator(workCenterPresenceConsistencyPort, true);

        when(workCenterPresenceConsistencyPort.findPresencePeriodsByEmployeeIdOrderByStartDate(10L))
                .thenReturn(List.of(new PresencePeriod(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10))));

        assertThrows(
                WorkCenterPresenceCoverageGapException.class,
                () -> validator.validatePresenceCoverageIfRequired(
                        10L,
                        List.of(
                                workCenter(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5)),
                                workCenter(2, LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 10))
                        ),
                        "ESP",
                        "INTERNAL",
                        "EMP001"
                )
        );
    }

    private WorkCenter workCenter(int assignmentNumber, LocalDate startDate, LocalDate endDate) {
        return new WorkCenter(
                (long) assignmentNumber,
                10L,
                assignmentNumber,
                "MADRID_HQ",
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}