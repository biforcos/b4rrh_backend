package com.b4rrhh.employee.cost_center.application.usecase;

import java.time.LocalDate;

/**
 * Closes all active cost center lines for an employee on the termination date.
 * Does not fail if no active distribution exists.
 * Must be invoked from the employee termination lifecycle.
 */
public interface CloseActiveCostCenterDistributionAtTerminationUseCase {

    void closeIfPresent(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate terminationDate
    );
}
