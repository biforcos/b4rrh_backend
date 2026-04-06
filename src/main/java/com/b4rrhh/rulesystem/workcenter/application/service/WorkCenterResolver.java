package com.b4rrhh.rulesystem.workcenter.application.service;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import com.b4rrhh.rulesystem.workcenter.domain.exception.WorkCenterNotApplicableException;
import com.b4rrhh.rulesystem.workcenter.domain.exception.WorkCenterNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkCenterResolver {

    private final RuleEntityRepository ruleEntityRepository;

    public WorkCenterResolver(RuleEntityRepository ruleEntityRepository) {
        this.ruleEntityRepository = ruleEntityRepository;
    }

    public RuleEntity resolveApplicableToday(String ruleSystemCode, String workCenterCode) {
        LocalDate referenceDate = LocalDate.now();

        return ruleEntityRepository
                .findApplicableByBusinessKey(
                        ruleSystemCode,
                        WorkCenterRuleEntityTypeCodes.WORK_CENTER,
                        workCenterCode,
                        referenceDate
                )
                .orElseGet(() -> {
                    boolean exists = !ruleEntityRepository
                            .findByFilters(ruleSystemCode, WorkCenterRuleEntityTypeCodes.WORK_CENTER, workCenterCode, null, null)
                            .isEmpty();

                    if (exists) {
                        throw new WorkCenterNotApplicableException(ruleSystemCode, workCenterCode, referenceDate);
                    }

                    throw new WorkCenterNotFoundException(ruleSystemCode, workCenterCode);
                });
    }
}