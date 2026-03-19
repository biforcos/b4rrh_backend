package com.b4rrhh.employee.contract.application.port;

import java.util.Optional;

public interface EmployeeContractLookupPort {

    Optional<EmployeeContractContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );

    Optional<EmployeeContractContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
