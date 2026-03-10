package com.b4rrhh.employee.presence.domain.exception;

public class PresenceCatalogValueInvalidException extends RuntimeException {

    public PresenceCatalogValueInvalidException(String fieldName, String value) {
        super(fieldName + " is invalid: " + value);
    }
}
