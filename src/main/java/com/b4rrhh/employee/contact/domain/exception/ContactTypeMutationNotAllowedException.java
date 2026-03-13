package com.b4rrhh.employee.contact.domain.exception;

public class ContactTypeMutationNotAllowedException extends RuntimeException {

    public ContactTypeMutationNotAllowedException(String contactTypeCode) {
        super("contactTypeCode mutation is not allowed for contactTypeCode=" + contactTypeCode);
    }
}
