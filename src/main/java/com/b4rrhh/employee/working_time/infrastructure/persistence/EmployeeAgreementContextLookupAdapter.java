package com.b4rrhh.employee.working_time.infrastructure.persistence;

import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Resolves the agreement context (ruleSystemCode + agreementCode) for an employee at a given date
 * by looking up the valid labor classification record.
 *
 * Uses JPA repository temporal query ordered by startDate descending.
 */
@Component
public class EmployeeAgreementContextLookupAdapter implements EmployeeAgreementContextLookupPort {

    private final EmployeeAgreementContextRepository repository;

    public EmployeeAgreementContextLookupAdapter(EmployeeAgreementContextRepository repository) {
        this.repository = repository;
    }

    @Override
    public EmployeeAgreementContext resolveContext(Long employeeId, LocalDate effectiveDate) {
        return repository.findLatestValidByEmployeeIdAndEffectiveDate(
                        employeeId,
                        effectiveDate,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No valid labor classification found for employee " + employeeId + " at " + effectiveDate
                ));
    }
}
