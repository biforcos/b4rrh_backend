package com.b4rrhh.employee.address.infrastructure.persistence;

import com.b4rrhh.employee.address.application.port.AddressCatalogReadPort;
import com.b4rrhh.employee.address.application.usecase.AddressRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddressCatalogReadAdapter implements AddressCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public AddressCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findAddressTypeName(String ruleSystemCode, String addressTypeCode) {
        String normalizedRuleSystemCode = normalizeToUppercase(ruleSystemCode);
        String normalizedAddressTypeCode = normalizeToUppercase(addressTypeCode);
        if (normalizedRuleSystemCode == null || normalizedAddressTypeCode == null) {
            return Optional.empty();
        }

        return ruleEntityRepository
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                        AddressRuleEntityTypeCodes.EMPLOYEE_ADDRESS_TYPE,
                        normalizedAddressTypeCode
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
