package com.b4rrhh.employee.cost_center.domain.exception;

public class CostCenterCatalogValueInvalidException extends RuntimeException {

    public CostCenterCatalogValueInvalidException(String fieldName, String value) {
        super("Invalid catalog value for " + fieldName + ": " + value);
    }
}
