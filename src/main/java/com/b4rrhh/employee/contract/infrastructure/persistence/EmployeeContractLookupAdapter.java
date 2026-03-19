package com.b4rrhh.employee.contract.infrastructure.persistence;

import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeeContractLookupAdapter implements EmployeeContractLookupPort {

    private final EntityManager entityManager;

    public EmployeeContractLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<EmployeeContractContext> findByBusinessKey(
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
    public Optional<EmployeeContractContext> findByBusinessKeyForUpdate(
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

    private Optional<EmployeeContractContext> mapResult(List<?> rows) {
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object row = rows.get(0);
        if (!(row instanceof Object[] columns) || columns.length < 4) {
            throw new IllegalStateException("Unexpected row shape for contract employee lookup query");
        }

        Long id = ((Number) columns[0]).longValue();
        String ruleSystemCode = (String) columns[1];
        String employeeTypeCode = (String) columns[2];
        String employeeNumber = (String) columns[3];

        return Optional.of(new EmployeeContractContext(id, ruleSystemCode, employeeTypeCode, employeeNumber));
    }
}
