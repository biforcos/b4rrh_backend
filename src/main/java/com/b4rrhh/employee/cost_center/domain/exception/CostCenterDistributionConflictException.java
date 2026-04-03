package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterDistributionConflictException extends RuntimeException {

    public CostCenterDistributionConflictException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate
    ) {
        super("An active cost center distribution window already exists at startDate="
                + startDate
                + " for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ". Use replace-from-date to change an existing distribution.");
    }
}
