package com.b4rrhh.payroll.basesalary.infrastructure.persistence;

import com.b4rrhh.payroll.basesalary.domain.PayrollObjectActivationLookupPort;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository.PayrollObjectActivationRepository;
import org.springframework.stereotype.Component;

@Component
public class PayrollObjectActivationLookupAdapter implements PayrollObjectActivationLookupPort {

    private final PayrollObjectActivationRepository repository;

    public PayrollObjectActivationLookupAdapter(PayrollObjectActivationRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean isActive(
            String ruleSystemCode,
            String ownerTypeCode,
            String ownerCode,
            String targetObjectTypeCode,
            String targetObjectCode
    ) {
        return repository.existsByRuleSystemCodeAndOwnerTypeCodeAndOwnerCodeAndTargetObjectTypeCodeAndTargetObjectCodeAndActiveTrue(
                ruleSystemCode,
                ownerTypeCode,
                ownerCode,
                targetObjectTypeCode,
                targetObjectCode
        );
    }
}
