package com.b4rrhh.employee.labor_classification.infrastructure.persistence;

import com.b4rrhh.employee.labor_classification.application.port.LaborClassificationCatalogReadPort;
import com.b4rrhh.employee.labor_classification.application.usecase.LaborClassificationRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LaborClassificationCatalogReadAdapter implements LaborClassificationCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;
    private final AgreementCategoryProfileRepository agreementCategoryProfileRepository;

    public LaborClassificationCatalogReadAdapter(
            RuleEntityRepository ruleEntityRepository,
            AgreementCategoryProfileRepository agreementCategoryProfileRepository
    ) {
        this.ruleEntityRepository = ruleEntityRepository;
        this.agreementCategoryProfileRepository = agreementCategoryProfileRepository;
    }

    @Override
    public Optional<String> findAgreementName(String ruleSystemCode, String agreementCode) {
        return findRuleEntityName(ruleSystemCode, LaborClassificationRuleEntityTypeCodes.AGREEMENT, agreementCode);
    }

    @Override
    public Optional<String> findAgreementCategoryName(String ruleSystemCode, String agreementCategoryCode) {
        return findRuleEntityName(
                ruleSystemCode,
                LaborClassificationRuleEntityTypeCodes.AGREEMENT_CATEGORY,
                agreementCategoryCode
        );
    }

    @Override
    public Optional<String> findGrupoCotizacionCode(String ruleSystemCode, String agreementCategoryCode) {
        if (agreementCategoryCode == null || agreementCategoryCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return agreementCategoryProfileRepository.findGrupoCotizacionCodeByCategoryCode(
                ruleSystemCode.trim().toUpperCase(),
                agreementCategoryCode.trim().toUpperCase()
        );
    }

    private Optional<String> findRuleEntityName(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedCode = normalizeToUppercase(code);
        if (normalizedRuleSystemCode == null || normalizedCode == null) {
            return Optional.empty();
        }

        return ruleEntityRepository.findByBusinessKey(normalizedRuleSystemCode, ruleEntityTypeCode, normalizedCode)
                .map(entity -> entity.getName() == null ? null : entity.getName().trim())
                .filter(name -> name != null && !name.isEmpty());
    }

    private String normalizeToUppercase(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }
}