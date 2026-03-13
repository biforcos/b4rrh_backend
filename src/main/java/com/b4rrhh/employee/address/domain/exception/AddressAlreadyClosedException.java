package com.b4rrhh.employee.address.domain.exception;

public class AddressAlreadyClosedException extends RuntimeException {

    public AddressAlreadyClosedException(Integer addressNumber) {
        super("Address is already closed for addressNumber=" + addressNumber);
    }
}
