package com.b4rrhh.employee.labor_classification.application.port;

import java.util.Optional;

public interface LaborClassificationCatalogReadPort {

    Optional<String> findAgreementName(String ruleSystemCode, String agreementCode);

    Optional<String> findAgreementCategoryName(String ruleSystemCode, String agreementCategoryCode);

    Optional<String> findGrupoCotizacionCode(String ruleSystemCode, String agreementCategoryCode);
}