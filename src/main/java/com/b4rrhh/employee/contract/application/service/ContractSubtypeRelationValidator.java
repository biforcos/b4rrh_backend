package com.b4rrhh.employee.contract.application.service;

import com.b4rrhh.employee.contract.application.port.ContractSubtypeRelationLookupPort;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ContractSubtypeRelationValidator {

    private final ContractSubtypeRelationLookupPort contractSubtypeRelationLookupPort;

    public ContractSubtypeRelationValidator(ContractSubtypeRelationLookupPort contractSubtypeRelationLookupPort) {
        this.contractSubtypeRelationLookupPort = contractSubtypeRelationLookupPort;
    }

    public void validateContractSubtypeRelation(
            String ruleSystemCode,
            String contractCode,
            String contractSubtypeCode,
            LocalDate referenceDate
    ) {
        boolean relationExists = contractSubtypeRelationLookupPort.existsActiveRelation(
                ruleSystemCode,
                contractCode,
                contractSubtypeCode,
                referenceDate
        );

        if (!relationExists) {
            throw new ContractSubtypeRelationInvalidException(
                    ruleSystemCode,
                    contractCode,
                    contractSubtypeCode,
                    referenceDate
            );
        }
    }
}
