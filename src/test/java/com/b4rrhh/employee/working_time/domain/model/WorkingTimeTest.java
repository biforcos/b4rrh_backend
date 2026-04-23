package com.b4rrhh.employee.working_time.domain.model;

import com.b4rrhh.employee.working_time.application.service.StandardWorkingTimeDerivationPolicy;
import com.b4rrhh.employee.working_time.domain.exception.InvalidWorkingTimeDateRangeException;
import com.b4rrhh.employee.working_time.domain.exception.InvalidWorkingTimePercentageException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeAlreadyClosedException;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkingTimeTest {

    @Test
    void closeReturnsClosedWorkingTimeWhenActive() {
        WorkingTime workingTime = workingTime(LocalDate.of(2026, 1, 10), null, new BigDecimal("50"));

        WorkingTime closed = workingTime.close(LocalDate.of(2026, 1, 20));

        assertEquals(LocalDate.of(2026, 1, 20), closed.getEndDate());
        assertEquals(workingTime.getWorkingTimeNumber(), closed.getWorkingTimeNumber());
    }

    @Test
    void closeRejectsAlreadyClosedWorkingTime() {
        WorkingTime workingTime = workingTime(
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 20),
                new BigDecimal("50")
        );

        assertThrows(WorkingTimeAlreadyClosedException.class, () -> workingTime.close(LocalDate.of(2026, 1, 21)));
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(
                InvalidWorkingTimeDateRangeException.class,
                () -> workingTime(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 31), new BigDecimal("50"))
        );
    }

    @Test
    void rejectsPercentageLessThanOrEqualToZero() {
        assertThrows(
                InvalidWorkingTimePercentageException.class,
                () -> workingTime(LocalDate.of(2026, 2, 1), null, BigDecimal.ZERO)
        );
    }

    @Test
    void rejectsPercentageGreaterThanOneHundred() {
        assertThrows(
                InvalidWorkingTimePercentageException.class,
                () -> workingTime(LocalDate.of(2026, 2, 1), null, new BigDecimal("100.01"))
        );
    }

    @Test
    void allowsOneDayPeriods() {
        WorkingTime workingTime = workingTime(
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 1),
                new BigDecimal("50")
        );

        assertEquals(LocalDate.of(2026, 2, 1), workingTime.getEndDate());
    }

    @Test
    void rejectsInconsistentDerivedHoursForPercentage() {
        assertThrows(
                InvalidWorkingTimePercentageException.class,
                () -> WorkingTime.create(
                        10L,
                        1,
                        LocalDate.of(2026, 2, 1),
                        null,
                        new BigDecimal("50"),
                        new WorkingTimeDerivedHours(
                                new BigDecimal("21.00"),
                                new BigDecimal("4.00"),
                                new BigDecimal("83.33")
                        ),
                        new BigDecimal("1736"),
                        new StandardWorkingTimeDerivationPolicy()
                )
        );
    }

    @Test
    void rehydrateAcceptsHistoricalDerivedHoursThatDoNotMatchCurrentPolicy() {
        WorkingTime workingTime = WorkingTime.rehydrate(
                1L,
                10L,
                1,
                LocalDate.of(2026, 2, 1),
                null,
                new BigDecimal("50"),
                new WorkingTimeDerivedHours(
                        new BigDecimal("22.50"),
                        new BigDecimal("4.50"),
                        new BigDecimal("90.00")
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        assertEquals(new BigDecimal("22.50"), workingTime.getWeeklyHours());
        assertEquals(new BigDecimal("4.50"), workingTime.getDailyHours());
        assertEquals(new BigDecimal("90.00"), workingTime.getMonthlyHours());
    }

    @Test
    void doesNotTreatCurrentFortyAndEightHourBasisAsAggregateInvariant() {
        WorkingTimeDerivationPolicy customPolicy = (percentage, annualHours) -> new WorkingTimeDerivedHours(
                new BigDecimal("60.00"),
                new BigDecimal("12.00"),
                new BigDecimal("250.00")
        );

        WorkingTime workingTime = WorkingTime.create(
                10L,
                1,
                LocalDate.of(2026, 2, 1),
                null,
                new BigDecimal("50"),
                new WorkingTimeDerivedHours(
                        new BigDecimal("60.00"),
                        new BigDecimal("12.00"),
                        new BigDecimal("250.00")
                ),
                new BigDecimal("1736"),
                customPolicy
        );

        assertEquals(new BigDecimal("60.00"), workingTime.getWeeklyHours());
        assertEquals(new BigDecimal("12.00"), workingTime.getDailyHours());
    }

    private WorkingTime workingTime(LocalDate startDate, LocalDate endDate, BigDecimal percentage) {
        WorkingTimeDerivedHours derivedHours = new StandardWorkingTimeDerivationPolicy()
                .derive(percentage, new BigDecimal("1736"));

        return WorkingTime.rehydrate(
                1L,
                10L,
                1,
                startDate,
                endDate,
                percentage,
                derivedHours,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}