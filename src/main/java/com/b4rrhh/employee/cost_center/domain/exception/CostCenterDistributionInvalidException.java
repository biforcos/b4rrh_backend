package com.b4rrhh.employee.cost_center.domain.exception;

public class CostCenterDistributionInvalidException extends RuntimeException {

    public CostCenterDistributionInvalidException(String reason) {
        super("Cost center distribution is invalid: " + reason);
    }
}
