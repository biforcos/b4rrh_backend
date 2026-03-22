package com.b4rrhh.employee.identifier.infrastructure.persistence;

import com.b4rrhh.employee.identifier.application.port.IdentifierCatalogReadPort;
import com.b4rrhh.employee.identifier.application.usecase.IdentifierRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdentifierCatalogReadAdapter implements IdentifierCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public IdentifierCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findIdentifierTypeName(String ruleSystemCode, String identifierTypeCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedIdentifierTypeCode = normalizeToUppercase(identifierTypeCode);
        if (normalizedRuleSystemCode == null || normalizedIdentifierTypeCode == null) {
            return Optional.empty();
        }

        return ruleEntityRepository
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                        IdentifierRuleEntityTypeCodes.EMPLOYEE_IDENTIFIER_TYPE,
                        normalizedIdentifierTypeCode
                )
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
