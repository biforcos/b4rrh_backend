package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterCatalogReadPort;
import com.b4rrhh.employee.workcenter.application.usecase.WorkCenterRuleEntityTypeCodes;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class WorkCenterCatalogReadAdapter implements WorkCenterCatalogReadPort {

    private final RuleEntityRepository ruleEntityRepository;
    private final WorkCenterCompanyLookupPort workCenterCompanyLookupPort;

    public WorkCenterCatalogReadAdapter(
            RuleEntityRepository ruleEntityRepository,
            WorkCenterCompanyLookupPort workCenterCompanyLookupPort
    ) {
        this.ruleEntityRepository = ruleEntityRepository;
        this.workCenterCompanyLookupPort = workCenterCompanyLookupPort;
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

    @Override
    public Optional<String> findWorkCenterCompanyCode(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
        String normalizedRuleSystemCode = normalizeRequiredUppercase("ruleSystemCode", ruleSystemCode);
        String normalizedWorkCenterCode = normalizeRequiredUppercase("workCenterCode", workCenterCode);

        return workCenterCompanyLookupPort.findCompanyCode(
                normalizedRuleSystemCode,
                normalizedWorkCenterCode,
                referenceDate
        );
    }

    @Override
    public Optional<String> findCompanyName(String ruleSystemCode, String companyCode) {
        String normalizedRuleSystemCode = normalizeRequiredUppercase("ruleSystemCode", ruleSystemCode);
        String normalizedCompanyCode = normalizeRequiredUppercase("companyCode", companyCode);

        return ruleEntityRepository
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                        WorkCenterRuleEntityTypeCodes.COMPANY,
                        normalizedCompanyCode
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
