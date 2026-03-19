package com.b4rrhh.employee.contract.domain.exception;

public class ContractInvalidException extends RuntimeException {

    public ContractInvalidException(String contractCode) {
        super("Invalid contractCode: " + contractCode);
    }
}
