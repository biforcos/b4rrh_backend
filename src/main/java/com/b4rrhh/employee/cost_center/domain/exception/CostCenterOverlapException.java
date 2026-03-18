package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterOverlapException extends RuntimeException {

    public CostCenterOverlapException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String costCenterCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        super("Cost center period overlaps for ruleSystemCode="
                + ruleSystemCode
                + ", employeeTypeCode="
                + employeeTypeCode
                + ", employeeNumber="
                + employeeNumber
                + ", costCenterCode="
                + costCenterCode
                + ", periodStart="
                + startDate
                + ", periodEnd="
                + endDate);
    }
}
