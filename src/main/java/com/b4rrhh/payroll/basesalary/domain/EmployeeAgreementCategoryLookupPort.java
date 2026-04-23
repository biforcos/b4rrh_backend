package com.b4rrhh.payroll.basesalary.domain;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Output port to resolve the employee agreement category at an effective date.
 */
public interface EmployeeAgreementCategoryLookupPort {

    Optional<String> resolveAgreementCategoryCode(Long employeeId, LocalDate effectiveDate);
}
