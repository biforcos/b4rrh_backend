package com.b4rrhh.employee.labor_classification.application.port;

import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;

import java.time.LocalDate;
import java.util.List;

public interface AgreementCategoryCatalogLookupPort {

    List<AgreementCategoryCatalogItem> listActiveCategoriesByAgreement(
            String ruleSystemCode,
        String agreementCode
    );

    List<AgreementCategoryCatalogItem> listActiveCategoriesByAgreementOnDate(
        String ruleSystemCode,
        String agreementCode,
            LocalDate referenceDate
    );
}
