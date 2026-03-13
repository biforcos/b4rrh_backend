package com.b4rrhh.employee.address.domain.exception;

public class AddressCatalogValueInvalidException extends RuntimeException {

    public AddressCatalogValueInvalidException(String fieldName, String value) {
        super(fieldName + " is invalid: " + value);
    }
}
