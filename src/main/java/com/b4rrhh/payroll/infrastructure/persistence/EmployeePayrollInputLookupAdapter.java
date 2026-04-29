package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.application.port.EmployeePayrollInputLookupPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmployeePayrollInputLookupAdapter implements EmployeePayrollInputLookupPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, BigDecimal> findInputsByPeriod(String ruleSystemCode, String employeeTypeCode,
                                                       String employeeNumber, int period) {
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT concept_code, quantity FROM employee.employee_payroll_input " +
                "WHERE rule_system_code = :rsc AND employee_type_code = :etc " +
                "AND employee_number = :en AND period = :period"
        )
                .setParameter("rsc", ruleSystemCode)
                .setParameter("etc", employeeTypeCode)
                .setParameter("en", employeeNumber)
                .setParameter("period", period)
                .getResultList();

        return rows.stream().collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (BigDecimal) row[1]
        ));
    }
}
