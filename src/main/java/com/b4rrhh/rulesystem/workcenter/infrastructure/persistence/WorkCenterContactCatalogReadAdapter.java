package com.b4rrhh.rulesystem.workcenter.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.workcenter.application.port.WorkCenterContactCatalogReadPort;
import com.b4rrhh.rulesystem.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkCenterContactCatalogReadAdapter implements WorkCenterContactCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public WorkCenterContactCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findContactTypeName(String ruleSystemCode, String contactTypeCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty() || contactTypeCode == null || contactTypeCode.trim().isEmpty()) {
            return Optional.empty();
        }

        return ruleEntityRepository
                .findByBusinessKey(
                        ruleSystemCode.trim().toUpperCase(),
                        WorkCenterRuleEntityTypeCodes.CONTACT_TYPE,
                        contactTypeCode.trim().toUpperCase()
                )
                .map(entity -> entity.getName() == null ? null : entity.getName().trim())
                .filter(name -> name != null && !name.isEmpty());
    }
}