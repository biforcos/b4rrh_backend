package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterAllocationNotFoundException extends RuntimeException {

    public CostCenterAllocationNotFoundException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String costCenterCode,
            LocalDate startDate
    ) {
        super("Cost center allocation not found for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", costCenterCode="
                + costCenterCode
                + ", startDate="
                + startDate);
    }
}
