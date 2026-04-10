package com.b4rrhh.authorization.domain.exception;

public class SecuredResourceNotFoundException extends RuntimeException {

    public SecuredResourceNotFoundException(String resourceCode) {
        super("Secured resource not found: resourceCode=" + resourceCode);
    }
}
