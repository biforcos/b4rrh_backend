package com.b4rrhh.employee.labor_classification.application.port;

import java.time.LocalDate;

public interface AgreementCategoryRelationLookupPort {

    boolean existsActiveRelation(
            String ruleSystemCode,
            String agreementCode,
            String agreementCategoryCode,
            LocalDate referenceDate
    );
}
