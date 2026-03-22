package com.b4rrhh.employee.contract.infrastructure.persistence;

import com.b4rrhh.employee.contract.application.port.ContractCatalogReadPort;
import com.b4rrhh.employee.contract.application.usecase.ContractRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContractCatalogReadAdapter implements ContractCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public ContractCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findContractTypeName(String ruleSystemCode, String contractCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedContractCode = normalizeToUppercase(contractCode);
        if (normalizedRuleSystemCode == null || normalizedContractCode == null) {
            return Optional.empty();
        }

        return findCatalogName(
                normalizedRuleSystemCode,
                ContractRuleEntityTypeCodes.CONTRACT,
                normalizedContractCode
        );
    }

    @Override
    public Optional<String> findContractSubtypeName(String ruleSystemCode, String contractSubtypeCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedContractSubtypeCode = normalizeToUppercase(contractSubtypeCode);
        if (normalizedRuleSystemCode == null || normalizedContractSubtypeCode == null) {
            return Optional.empty();
        }

        return findCatalogName(
                normalizedRuleSystemCode,
                ContractRuleEntityTypeCodes.CONTRACT_SUBTYPE,
                normalizedContractSubtypeCode
        );
    }

    private Optional<String> findCatalogName(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        return ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, ruleEntityTypeCode, code)
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
