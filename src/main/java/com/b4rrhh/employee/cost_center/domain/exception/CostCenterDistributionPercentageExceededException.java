package com.b4rrhh.employee.cost_center.domain.exception;

import java.math.BigDecimal;

public class CostCenterDistributionPercentageExceededException extends RuntimeException {

    public CostCenterDistributionPercentageExceededException(BigDecimal total) {
        super("Total allocation percentage for the distribution exceeds 100: " + total);
    }
}
