package com.b4rrhh.employee.identifier.domain.exception;

public class IdentifierSpanishNationalIdInvalidException extends IdentifierValueInvalidException {

    public static final String FUNCTIONAL_CODE = "INVALID_SPANISH_NATIONAL_ID";

    public IdentifierSpanishNationalIdInvalidException() {
        super(FUNCTIONAL_CODE + ": El DNI espanol no es valido para el formato o la letra informada.");
    }
}