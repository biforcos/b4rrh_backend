package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterCatalogValueInvalidException extends RuntimeException {

    public WorkCenterCatalogValueInvalidException(String fieldName, String value) {
        super(fieldName + " is invalid: " + value);
    }
}