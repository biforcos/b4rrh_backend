package com.b4rrhh.employee.contract.domain.exception;

import java.time.LocalDate;

public class ContractAlreadyClosedException extends RuntimeException {

    public ContractAlreadyClosedException(LocalDate startDate) {
        super("Contract is already closed for startDate=" + startDate);
    }
}
