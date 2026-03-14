package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeWorkCenterLookupAdapter implements EmployeeWorkCenterLookupPort {

    private final EntityManager entityManager;

    public EmployeeWorkCenterLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<EmployeeWorkCenterContext> findByBusinessKey(
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
    public Optional<EmployeeWorkCenterContext> findByBusinessKeyForUpdate(
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

    private Optional<EmployeeWorkCenterContext> mapResult(List<?> rows) {
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object row = rows.get(0);
        if (!(row instanceof Object[] columns) || columns.length < 4) {
            throw new IllegalStateException("Unexpected row shape for employee work center lookup query");
        }

        Long id = ((Number) columns[0]).longValue();
        String ruleSystemCode = (String) columns[1];
        String employeeTypeCode = (String) columns[2];
        String employeeNumber = (String) columns[3];

        return Optional.of(new EmployeeWorkCenterContext(id, ruleSystemCode, employeeTypeCode, employeeNumber));
    }
}