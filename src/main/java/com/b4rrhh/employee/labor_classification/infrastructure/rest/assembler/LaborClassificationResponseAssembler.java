package com.b4rrhh.employee.labor_classification.infrastructure.rest.assembler;

import com.b4rrhh.employee.labor_classification.application.port.LaborClassificationCatalogReadPort;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.LaborClassificationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LaborClassificationResponseAssembler {

    private final LaborClassificationCatalogReadPort laborClassificationCatalogReadPort;

    public LaborClassificationResponseAssembler(LaborClassificationCatalogReadPort laborClassificationCatalogReadPort) {
        this.laborClassificationCatalogReadPort = laborClassificationCatalogReadPort;
    }

    public LaborClassificationResponse toResponse(String ruleSystemCode, LaborClassification laborClassification) {
        String agreementName = laborClassificationCatalogReadPort
                .findAgreementName(ruleSystemCode, laborClassification.getAgreementCode())
                .orElse(null);
        String agreementCategoryName = laborClassificationCatalogReadPort
                .findAgreementCategoryName(ruleSystemCode, laborClassification.getAgreementCategoryCode())
                .orElse(null);
        String grupoCotizacionCode = laborClassificationCatalogReadPort
                .findGrupoCotizacionCode(ruleSystemCode, laborClassification.getAgreementCategoryCode())
                .orElse(null);

        return new LaborClassificationResponse(
                laborClassification.getAgreementCode(),
                agreementName,
                laborClassification.getAgreementCategoryCode(),
                agreementCategoryName,
                grupoCotizacionCode,
                laborClassification.getStartDate(),
                laborClassification.getEndDate()
        );
    }

    public List<LaborClassificationResponse> toResponseList(
            String ruleSystemCode,
            List<LaborClassification> laborClassifications
    ) {
        return laborClassifications.stream()
                .map(laborClassification -> toResponse(ruleSystemCode, laborClassification))
                .toList();
    }
}