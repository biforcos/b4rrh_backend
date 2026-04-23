package com.b4rrhh.payroll.basesalary.infrastructure.persistence;

import com.b4rrhh.payroll.basesalary.domain.PayrollTableRowLookupPort;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity.PayrollTableRowEntity;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository.PayrollTableRowRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Adapter implementing PayrollTableRowLookupPort.
 * Resolves payroll table row values from persistence.
 */
@Component
public class PayrollTableRowLookupAdapter implements PayrollTableRowLookupPort {

    private final PayrollTableRowRepository repository;

    public PayrollTableRowLookupAdapter(PayrollTableRowRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<BigDecimal> resolveMonthlyValue(
            String ruleSystemCode,
            String tableCode,
            String searchCode,
            LocalDate effectiveDate
    ) {
        return repository.findLatestValidByRuleSystemCodeAndTableCodeAndSearchCodeAndEffectiveDate(
                ruleSystemCode,
                tableCode,
                searchCode,
                effectiveDate,
                PageRequest.of(0, 1)
        ).stream().findFirst().map(PayrollTableRowEntity::getMonthlyValue);
    }
}
