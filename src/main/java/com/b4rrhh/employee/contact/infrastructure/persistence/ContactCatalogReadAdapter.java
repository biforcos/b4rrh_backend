package com.b4rrhh.employee.contact.infrastructure.persistence;

import com.b4rrhh.employee.contact.application.port.ContactCatalogReadPort;
import com.b4rrhh.employee.contact.application.usecase.ContactRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ContactCatalogReadAdapter implements ContactCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public ContactCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findContactTypeName(String ruleSystemCode, String contactTypeCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedContactTypeCode = normalizeToUppercase(contactTypeCode);
        if (normalizedRuleSystemCode == null || normalizedContactTypeCode == null) {
            return Optional.empty();
        }

        return ruleEntityRepository
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                ContactRuleEntityTypeCodes.CONTACT_TYPE,
                        normalizedContactTypeCode
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
