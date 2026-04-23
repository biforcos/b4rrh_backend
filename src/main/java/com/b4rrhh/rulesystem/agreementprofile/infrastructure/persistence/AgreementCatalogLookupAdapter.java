package com.b4rrhh.rulesystem.agreementprofile.infrastructure.persistence;

import com.b4rrhh.rulesystem.agreementprofile.application.port.AgreementCatalogLookupPort;
import com.b4rrhh.rulesystem.infrastructure.persistence.SpringDataRuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves AGREEMENT rule entity IDs by business key using JPA.
 * Uses the existing SpringDataRuleEntityRepository which maps the rulesystem.rule_entity table.
 */
@Component
public class AgreementCatalogLookupAdapter implements AgreementCatalogLookupPort {

    private static final String AGREEMENT_TYPE_CODE = "AGREEMENT";

    private final SpringDataRuleEntityRepository ruleEntityRepository;

    public AgreementCatalogLookupAdapter(SpringDataRuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<Long> findAgreementRuleEntityId(String ruleSystemCode, String agreementCode) {
        return ruleEntityRepository
                .findByRuleSystemCodeAndRuleEntityTypeCodeAndCode(ruleSystemCode, AGREEMENT_TYPE_CODE, agreementCode)
                .map(entity -> entity.getId());
    }
}
