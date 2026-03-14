package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierCatalogValueInvalidException extends RuntimeException {

    public IdentifierCatalogValueInvalidException(String fieldName, String value) {
        super(fieldName + " is invalid: " + value);
    }
}
