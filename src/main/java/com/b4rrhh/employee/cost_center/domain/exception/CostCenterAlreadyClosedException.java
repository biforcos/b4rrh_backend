package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterAlreadyClosedException extends RuntimeException {

    public CostCenterAlreadyClosedException(String costCenterCode, LocalDate startDate) {
        super("Cost center allocation is already closed for costCenterCode="
                + costCenterCode
                + ", startDate="
                + startDate);
    }
}
