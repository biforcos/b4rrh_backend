package com.b4rrhh.employee.cost_center.infrastructure.persistence;

import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterContext;
import com.b4rrhh.employee.cost_center.application.port.EmployeeCostCenterLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeCostCenterLookupAdapter implements EmployeeCostCenterLookupPort {

    private final EntityManager entityManager;

    public EmployeeCostCenterLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<EmployeeCostCenterContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<?> rows = entityManager.createNativeQuery("""
                select id, rule_system_code, employee_type_code, employee_number
                from employee.employee
                where upper(trim(rule_system_code)) = :ruleSystemCode
                  and upper(trim(employee_type_code)) = :employeeTypeCode
                  and trim(employee_number) = :employeeNumber
                """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .getResultList();

        return mapResult(rows);
    }

    @Override
    public Optional<EmployeeCostCenterContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<?> rows = entityManager.createNativeQuery("""
                select id, rule_system_code, employee_type_code, employee_number
                from employee.employee
                where upper(trim(rule_system_code)) = :ruleSystemCode
                  and upper(trim(employee_type_code)) = :employeeTypeCode
                  and trim(employee_number) = :employeeNumber
                for update
                """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .getResultList();

        return mapResult(rows);
    }

    private Optional<EmployeeCostCenterContext> mapResult(List<?> rows) {
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object row = rows.get(0);
        if (!(row instanceof Object[] columns) || columns.length < 4) {
            throw new IllegalStateException("Unexpected row shape for employee cost center lookup query");
        }

        Long id = ((Number) columns[0]).longValue();
        String ruleSystemCode = (String) columns[1];
        String employeeTypeCode = (String) columns[2];
        String employeeNumber = (String) columns[3];

        return Optional.of(new EmployeeCostCenterContext(id, ruleSystemCode, employeeTypeCode, employeeNumber));
    }
}
