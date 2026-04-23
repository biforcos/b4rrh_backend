package com.b4rrhh.payroll.basesalary.infrastructure.persistence;

import com.b4rrhh.payroll.basesalary.domain.PayrollObjectBindingLookupPort;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository.PayrollObjectBindingRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter implementing PayrollObjectBindingLookupPort.
 * Resolves payroll object bindings from persistence.
 */
@Component
public class PayrollObjectBindingLookupAdapter implements PayrollObjectBindingLookupPort {

    private final PayrollObjectBindingRepository repository;

    public PayrollObjectBindingLookupAdapter(PayrollObjectBindingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> resolveBoundObjectCode(
            String ruleSystemCode,
            String ownerTypeCode,
            String ownerCode,
            String bindingRoleCode
    ) {
        return repository.findByRuleSystemCodeAndOwnerTypeCodeAndOwnerCodeAndBindingRoleCodeAndBoundObjectTypeCodeAndActiveTrue(
                ruleSystemCode,
                ownerTypeCode,
                ownerCode,
                bindingRoleCode,
                "TABLE"
        ).map(binding -> binding.getBoundObjectCode());
    }
}
