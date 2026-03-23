package com.b4rrhh.employee.contract.application.port;

import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;

import java.time.LocalDate;
import java.util.List;

public interface ContractSubtypeCatalogLookupPort {

    List<ContractSubtypeCatalogItem> listActiveSubtypesByContractType(
            String ruleSystemCode,
            String contractTypeCode
    );

    List<ContractSubtypeCatalogItem> listActiveSubtypesByContractTypeOnDate(
            String ruleSystemCode,
            String contractTypeCode,
            LocalDate referenceDate
    );
}