package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.workcenter.infrastructure.persistence.SpringDataWorkCenterProfileRepository;
import com.b4rrhh.rulesystem.workcenter.infrastructure.persistence.WorkCenterProfileEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class WorkCenterCompanyLookupAdapter implements WorkCenterCompanyLookupPort {

    private final RuleEntityRepository ruleEntityRepository;
    private final SpringDataWorkCenterProfileRepository springDataWorkCenterProfileRepository;

    public WorkCenterCompanyLookupAdapter(
            RuleEntityRepository ruleEntityRepository,
            SpringDataWorkCenterProfileRepository springDataWorkCenterProfileRepository
    ) {
        this.ruleEntityRepository = ruleEntityRepository;
        this.springDataWorkCenterProfileRepository = springDataWorkCenterProfileRepository;
    }

    @Override
    public Optional<String> findCompanyCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
        return ruleEntityRepository
                .findApplicableByBusinessKey(
                        ruleSystemCode,
                        WorkCenterRuleEntityTypeCodes.WORK_CENTER,
                        workCenterCode,
                        referenceDate
                )
                .flatMap(ruleEntity -> springDataWorkCenterProfileRepository.findByWorkCenterRuleEntityId(ruleEntity.getId()))
                .map(WorkCenterProfileEntity::getCompanyCode)
                .map(String::trim)
                .filter(companyCode -> !companyCode.isEmpty())
                .map(String::toUpperCase);
    }
}