package com.b4rrhh.employee.contact.domain.exception;

public class ContactCatalogValueInvalidException extends RuntimeException {

    public ContactCatalogValueInvalidException(String fieldName, String value) {
        super(fieldName + " is invalid: " + value);
    }
}
