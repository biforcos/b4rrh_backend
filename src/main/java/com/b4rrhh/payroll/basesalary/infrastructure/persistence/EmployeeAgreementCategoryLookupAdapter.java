package com.b4rrhh.payroll.basesalary.infrastructure.persistence;

import com.b4rrhh.payroll.basesalary.domain.EmployeeAgreementCategoryLookupPort;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository.EmployeeAgreementCategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class EmployeeAgreementCategoryLookupAdapter implements EmployeeAgreementCategoryLookupPort {

    private final EmployeeAgreementCategoryRepository repository;

    public EmployeeAgreementCategoryLookupAdapter(EmployeeAgreementCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<String> resolveAgreementCategoryCode(Long employeeId, LocalDate effectiveDate) {
        return repository.findAgreementCategoryCodesByEmployeeAndEffectiveDate(
                        employeeId,
                        effectiveDate,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst();
    }
}
