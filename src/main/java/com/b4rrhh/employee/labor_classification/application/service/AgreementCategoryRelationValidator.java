package com.b4rrhh.employee.labor_classification.application.service;

import com.b4rrhh.employee.labor_classification.application.port.AgreementCategoryRelationLookupPort;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AgreementCategoryRelationValidator {

    private final AgreementCategoryRelationLookupPort agreementCategoryRelationLookupPort;

    public AgreementCategoryRelationValidator(AgreementCategoryRelationLookupPort agreementCategoryRelationLookupPort) {
        this.agreementCategoryRelationLookupPort = agreementCategoryRelationLookupPort;
    }

    public void validateAgreementCategoryRelation(
            String ruleSystemCode,
            String agreementCode,
            String agreementCategoryCode,
            LocalDate referenceDate
    ) {
        boolean relationExists = agreementCategoryRelationLookupPort.existsActiveRelation(
                ruleSystemCode,
                agreementCode,
                agreementCategoryCode,
                referenceDate
        );

        if (!relationExists) {
            throw new LaborClassificationAgreementCategoryRelationInvalidException(
                    ruleSystemCode,
                    agreementCode,
                    agreementCategoryCode,
                    referenceDate
            );
        }
    }
}
