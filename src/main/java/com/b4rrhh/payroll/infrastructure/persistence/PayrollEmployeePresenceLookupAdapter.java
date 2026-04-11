package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.application.port.PayrollEmployeePresenceContext;
import com.b4rrhh.payroll.application.port.PayrollEmployeePresenceLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PayrollEmployeePresenceLookupAdapter implements PayrollEmployeePresenceLookupPort {

    private final EntityManager entityManager;

    public PayrollEmployeePresenceLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<PayrollEmployeePresenceContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber
    ) {
        List<?> rows = entityManager.createNativeQuery("""
            select e.id,
                   p.id,
                   e.rule_system_code,
                   e.employee_type_code,
                   e.employee_number,
                   p.presence_number
              from employee.employee e
              join employee.presence p on p.employee_id = e.id
             where upper(trim(e.rule_system_code)) = :ruleSystemCode
               and upper(trim(e.employee_type_code)) = :employeeTypeCode
               and trim(e.employee_number) = :employeeNumber
               and p.presence_number = :presenceNumber
             for update
            """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .setParameter("presenceNumber", presenceNumber)
                .getResultList();

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object row = rows.get(0);
        if (!(row instanceof Object[] columns) || columns.length < 6) {
            throw new IllegalStateException("Unexpected row shape for payroll employee presence lookup query");
        }

        return Optional.of(new PayrollEmployeePresenceContext(
                ((Number) columns[0]).longValue(),
                ((Number) columns[1]).longValue(),
                (String) columns[2],
                (String) columns[3],
                (String) columns[4],
                ((Number) columns[5]).intValue()
        ));
    }
}