package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterCatalogReadPort;
import com.b4rrhh.employee.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkCenterCatalogReadAdapter implements WorkCenterCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;

    public WorkCenterCatalogReadAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public Optional<String> findWorkCenterName(String ruleSystemCode, String workCenterCode) {
        String normalizedRuleSystemCode = normalizeRequiredUppercase("ruleSystemCode", ruleSystemCode);
        String normalizedWorkCenterCode = normalizeRequiredUppercase("workCenterCode", workCenterCode);

        return ruleEntityRepository
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                        WorkCenterRuleEntityTypeCodes.WORK_CENTER,
                        normalizedWorkCenterCode
                )
                .map(entity -> entity.getName() == null ? null : entity.getName().trim())
                .filter(name -> name != null && !name.isEmpty());
    }

    private String normalizeRequiredUppercase(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        return value.trim().toUpperCase();
    }
}
