package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.application.port.PayrollLaunchEmployeeContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchPresenceContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchPresenceLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class PayrollLaunchPresenceLookupAdapter implements PayrollLaunchPresenceLookupPort {

    private final EntityManager entityManager;

    public PayrollLaunchPresenceLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<PayrollLaunchEmployeeContext> findEmployeesWithPresenceInPeriod(
            String ruleSystemCode,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        List<?> rows = entityManager.createNativeQuery("""
            select distinct e.employee_type_code,
                            e.employee_number
              from employee.employee e
              join employee.presence p on p.employee_id = e.id
             where upper(trim(e.rule_system_code)) = :ruleSystemCode
               and p.start_date <= :periodEnd
               and (p.end_date is null or p.end_date >= :periodStart)
             order by e.employee_type_code asc,
                      e.employee_number asc
            """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .getResultList();

        return rows.stream().map(this::toEmployeeContext).toList();
    }

    @Override
    public List<PayrollLaunchPresenceContext> findRelevantPresences(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        List<?> rows = entityManager.createNativeQuery("""
            select e.rule_system_code,
                   e.employee_type_code,
                   e.employee_number,
                   p.presence_number
              from employee.employee e
              join employee.presence p on p.employee_id = e.id
             where upper(trim(e.rule_system_code)) = :ruleSystemCode
               and upper(trim(e.employee_type_code)) = :employeeTypeCode
               and trim(e.employee_number) = :employeeNumber
               and p.start_date <= :periodEnd
               and (p.end_date is null or p.end_date >= :periodStart)
             order by p.start_date asc, p.presence_number asc
            """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .getResultList();

        return rows.stream().map(this::toContext).toList();
    }

    private PayrollLaunchPresenceContext toContext(Object row) {
        if (!(row instanceof Object[] columns) || columns.length < 4) {
            throw new IllegalStateException("Unexpected row shape for payroll launch presence lookup query");
        }
        return new PayrollLaunchPresenceContext(
                (String) columns[0],
                (String) columns[1],
                (String) columns[2],
                ((Number) columns[3]).intValue()
        );
    }

    private PayrollLaunchEmployeeContext toEmployeeContext(Object row) {
        if (!(row instanceof Object[] columns) || columns.length < 2) {
            throw new IllegalStateException("Unexpected row shape for payroll launch employee lookup query");
        }
        return new PayrollLaunchEmployeeContext(
                (String) columns[0],
                (String) columns[1]
        );
    }
}