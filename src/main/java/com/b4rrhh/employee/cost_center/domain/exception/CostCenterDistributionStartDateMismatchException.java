package com.b4rrhh.employee.cost_center.domain.exception;

import java.time.LocalDate;

public class CostCenterDistributionStartDateMismatchException extends RuntimeException {

    public CostCenterDistributionStartDateMismatchException(LocalDate expectedStartDate, LocalDate actualStartDate) {
        super("All lines in a distribution window must share the same startDate. Expected "
                + expectedStartDate + " but found " + actualStartDate);
    }
}
