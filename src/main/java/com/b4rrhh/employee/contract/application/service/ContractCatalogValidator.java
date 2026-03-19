package com.b4rrhh.employee.contract.application.service;

import com.b4rrhh.employee.contract.application.usecase.ContractRuleEntityTypeCodes;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ContractCatalogValidator {

    private static final int CONTRACT_CODE_LENGTH = 3;

    private final RuleEntityRepository ruleEntityRepository;

    public ContractCatalogValidator(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public String normalizeRequiredCode(String fieldName, String value) {
        String normalized = value == null ? null : value.trim().toUpperCase();

        if (normalized == null || normalized.isEmpty()) {
            if ("contractCode".equals(fieldName)) {
                throw new ContractInvalidException(String.valueOf(value));
            }
            if ("contractSubtypeCode".equals(fieldName)) {
                throw new ContractSubtypeInvalidException(String.valueOf(value));
            }
            throw new IllegalArgumentException(fieldName + " is required");
        }

        if (normalized.length() != CONTRACT_CODE_LENGTH) {
            if ("contractCode".equals(fieldName)) {
                throw new ContractInvalidException(normalized);
            }
            if ("contractSubtypeCode".equals(fieldName)) {
                throw new ContractSubtypeInvalidException(normalized);
            }
        }

        return normalized;
    }

    public void validateContractCode(String ruleSystemCode, String contractCode, LocalDate referenceDate) {
        RuleEntity ruleEntity = ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, ContractRuleEntityTypeCodes.CONTRACT, contractCode)
                .orElseThrow(() -> new ContractInvalidException(contractCode));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new ContractInvalidException(contractCode);
        }
    }

    public void validateContractSubtypeCode(
            String ruleSystemCode,
            String contractSubtypeCode,
            LocalDate referenceDate
    ) {
        RuleEntity ruleEntity = ruleEntityRepository
                .findByBusinessKey(
                        ruleSystemCode,
                        ContractRuleEntityTypeCodes.CONTRACT_SUBTYPE,
                        contractSubtypeCode
                )
                .orElseThrow(() -> new ContractSubtypeInvalidException(contractSubtypeCode));

        if (!ruleEntity.isActive() || !isDateApplicable(ruleEntity, referenceDate)) {
            throw new ContractSubtypeInvalidException(contractSubtypeCode);
        }
    }

    private boolean isDateApplicable(RuleEntity ruleEntity, LocalDate referenceDate) {
        if (referenceDate == null) {
            return true;
        }

        boolean startsBeforeOrOnDate = !referenceDate.isBefore(ruleEntity.getStartDate());
        boolean endsAfterOrOnDate = ruleEntity.getEndDate() == null || !referenceDate.isAfter(ruleEntity.getEndDate());

        return startsBeforeOrOnDate && endsAfterOrOnDate;
    }
}
