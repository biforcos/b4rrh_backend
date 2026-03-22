package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import com.b4rrhh.employee.workcenter.domain.port.RuleEntityValidationPort;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RuleEntityValidationAdapter implements RuleEntityValidationPort {

    private final RuleEntityRepository ruleEntityRepository;

    public RuleEntityValidationAdapter(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    public boolean existsActiveWorkCenterCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
        return ruleEntityRepository
                .findByBusinessKey(ruleSystemCode, WorkCenterRuleEntityTypeCodes.WORK_CENTER, workCenterCode)
                .map(entity -> entity.isActive() && isDateApplicable(entity, referenceDate))
                .orElse(false);
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
