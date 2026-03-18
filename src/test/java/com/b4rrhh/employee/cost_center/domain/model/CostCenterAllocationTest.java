package com.b4rrhh.employee.cost_center.domain.model;

import com.b4rrhh.employee.cost_center.domain.exception.InvalidAllocationPercentageException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidCostCenterDateRangeException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class CostCenterAllocationTest {

    @Test
    void rejectsInvalidAllocationPercentage() {
        assertThrows(
                InvalidAllocationPercentageException.class,
                () -> new CostCenterAllocation(
                        10L,
                        "CC01",
                        BigDecimal.ZERO,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );

        assertThrows(
                InvalidAllocationPercentageException.class,
                () -> new CostCenterAllocation(
                        10L,
                        "CC01",
                        new BigDecimal("100.01"),
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(
                InvalidCostCenterDateRangeException.class,
                () -> new CostCenterAllocation(
                        10L,
                        "CC01",
                        new BigDecimal("50"),
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 9)
                )
        );
    }
}
