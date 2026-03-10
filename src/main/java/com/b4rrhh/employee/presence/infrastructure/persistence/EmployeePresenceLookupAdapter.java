package com.b4rrhh.employee.presence.infrastructure.persistence;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeePresenceLookupAdapter implements EmployeePresenceLookupPort {

    private final EntityManager entityManager;

    public EmployeePresenceLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<EmployeePresenceContext> findById(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select id, rule_system_code
                from employee.employee
                where id = :employeeId
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return mapResult(rows);
    }

    @Override
    public Optional<EmployeePresenceContext> findByIdForUpdate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select id, rule_system_code
                from employee.employee
                where id = :employeeId
                for update
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return mapResult(rows);
    }

    @Override
    public Optional<EmployeePresenceContext> findByBusinessKey(String ruleSystemCode, String employeeNumber) {
        List<?> rows = entityManager.createNativeQuery("""
                select id, rule_system_code
                from employee.employee
                where upper(trim(rule_system_code)) = :ruleSystemCode
                  and trim(employee_number) = :employeeNumber
                """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeNumber", employeeNumber)
                .getResultList();

        return mapResult(rows);
    }

    private Optional<EmployeePresenceContext> mapResult(List<?> rows) {
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object row = rows.get(0);
        if (!(row instanceof Object[] columns) || columns.length < 2) {
            throw new IllegalStateException("Unexpected row shape for employee presence lookup query");
        }

        Long id = ((Number) columns[0]).longValue();
        String ruleSystemCode = (String) columns[1];

        return Optional.of(new EmployeePresenceContext(id, ruleSystemCode));
    }
}
