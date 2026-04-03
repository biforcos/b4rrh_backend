package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterDistributionNotFoundException extends RuntimeException {

    public CostCenterDistributionNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate startDate
    ) {
        super("No cost center distribution window found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", startDate="
                + startDate);
    }

    public CostCenterDistributionNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String context
    ) {
        super("No cost center distribution window found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ": "
                + context);
    }
}
