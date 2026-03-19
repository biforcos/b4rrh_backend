package com.b4rrhh.employee.contract.domain.exception;

public class ContractSubtypeInvalidException extends RuntimeException {

    public ContractSubtypeInvalidException(String contractSubtypeCode) {
        super("Invalid contractSubtypeCode: " + contractSubtypeCode);
    }
}
