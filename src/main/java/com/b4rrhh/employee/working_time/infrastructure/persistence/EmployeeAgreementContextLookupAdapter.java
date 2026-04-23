package com.b4rrhh.employee.working_time.infrastructure.persistence;

import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Resolves the agreement context (ruleSystemCode + agreementCode) for an employee at a given date
 * by looking up the valid labor classification record.
 *
 * Native SQL is used here because:
 * - Effective-date range lookup (start_date ≤ date ≤ end_date) with ORDER BY + LIMIT is not
 *   straightforward in JPQL without result-set filtering in Java.
 * - A single join between employee.labor_classification and employee.employee retrieves
 *   both agreementCode and ruleSystemCode atomically.
 */
@Component
public class EmployeeAgreementContextLookupAdapter implements EmployeeAgreementContextLookupPort {

    private final EntityManager entityManager;

    public EmployeeAgreementContextLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EmployeeAgreementContext resolveContext(Long employeeId, LocalDate effectiveDate) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery("""
                        select elc.agreement_code, e.rule_system_code
                        from employee.labor_classification elc
                        join employee.employee e on e.id = elc.employee_id
                        where elc.employee_id = :employeeId
                          and elc.start_date <= :effectiveDate
                          and (elc.end_date is null or elc.end_date >= :effectiveDate)
                        order by elc.start_date desc
                        limit 1
                        """)
                .setParameter("employeeId", employeeId)
                .setParameter("effectiveDate", effectiveDate)
            .getResultList();

        if (rows.isEmpty()) {
            throw new IllegalStateException(
                "No valid labor classification found for employee " + employeeId + " at " + effectiveDate
            );
        }

        Object[] row = rows.get(0);
        String agreementCode = (String) row[0];
        String ruleSystemCode = (String) row[1];

        return new EmployeeAgreementContext(ruleSystemCode, agreementCode);
    }
}
