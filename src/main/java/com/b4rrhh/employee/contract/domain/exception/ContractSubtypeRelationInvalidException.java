package com.b4rrhh.employee.contract.domain.exception;

import java.time.LocalDate;

public class ContractSubtypeRelationInvalidException extends RuntimeException {

    public ContractSubtypeRelationInvalidException(
            String ruleSystemCode,
            String contractCode,
            String contractSubtypeCode,
            LocalDate referenceDate
    ) {
        super("Invalid contract-subtype relation for ruleSystemCode="
                + ruleSystemCode
                + ", contractCode="
                + contractCode
                + ", contractSubtypeCode="
                + contractSubtypeCode
                + ", referenceDate="
                + referenceDate);
    }
}
