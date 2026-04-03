package com.b4rrhh.employee.cost_center.infrastructure.persistence;

import com.b4rrhh.employee.cost_center.application.port.CostCenterCatalogReadPort;
import com.b4rrhh.employee.cost_center.application.usecase.CostCenterRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CostCenterCatalogReadAdapter implements CostCenterCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public CostCenterCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findCostCenterName(String ruleSystemCode, String costCenterCode) {
        return ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, CostCenterRuleEntityTypeCodes.COST_CENTER, costCenterCode)
                .map(entity -> entity.getName());
    }
}
