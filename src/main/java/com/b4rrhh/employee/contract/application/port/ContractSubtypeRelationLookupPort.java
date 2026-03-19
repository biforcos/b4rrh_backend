package com.b4rrhh.employee.contract.application.port;

import java.time.LocalDate;

public interface ContractSubtypeRelationLookupPort {

    boolean existsActiveRelation(
            String ruleSystemCode,
            String contractCode,
            String contractSubtypeCode,
            LocalDate referenceDate
    );
}
