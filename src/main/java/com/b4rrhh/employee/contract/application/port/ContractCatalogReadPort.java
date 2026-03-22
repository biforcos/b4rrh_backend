package com.b4rrhh.employee.contract.application.port;

import java.util.Optional;

public interface ContractCatalogReadPort {

    Optional<String> findContractTypeName(String ruleSystemCode, String contractCode);

    Optional<String> findContractSubtypeName(String ruleSystemCode, String contractSubtypeCode);
}
