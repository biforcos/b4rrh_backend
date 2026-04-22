package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputLookupPort;
import com.b4rrhh.payroll.application.port.PayrollLaunchWorkingTimeWindowContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PayrollLaunchEligibleInputLookupAdapter implements PayrollLaunchEligibleInputLookupPort {

    private final EntityManager entityManager;

    public PayrollLaunchEligibleInputLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<PayrollLaunchEligibleInputContext> findByUnitAndPeriod(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        List<?> headerRows = entityManager.createNativeQuery("""
            select p.company_code,
                   lc.agreement_code
              from employee.employee e
              join employee.presence p
                on p.employee_id = e.id
         left join lateral (
                    select lcx.agreement_code
                      from employee.labor_classification lcx
                     where lcx.employee_id = e.id
                       and lcx.start_date <= :periodEnd
                       and (lcx.end_date is null or lcx.end_date >= :periodStart)
                     order by lcx.start_date desc, lcx.id desc
                     limit 1
                ) lc on true
             where upper(trim(e.rule_system_code)) = :ruleSystemCode
               and upper(trim(e.employee_type_code)) = :employeeTypeCode
               and trim(e.employee_number) = :employeeNumber
               and p.presence_number = :presenceNumber
               and p.start_date <= :periodEnd
               and (p.end_date is null or p.end_date >= :periodStart)
            """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .setParameter("presenceNumber", presenceNumber)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .getResultList();

        if (headerRows.isEmpty()) {
            return Optional.empty();
        }

        Object headerRow = headerRows.getFirst();
        if (!(headerRow instanceof Object[] columns) || columns.length < 2) {
            throw new IllegalStateException("Unexpected row shape for payroll launch eligible input header query");
        }

        String companyCode = (String) columns[0];
        String agreementCode = (String) columns[1];

        List<?> workingTimeRows = entityManager.createNativeQuery("""
            select wt.start_date,
                   wt.end_date,
                   wt.working_time_percentage
              from employee.employee e
              join employee.working_time wt
                on wt.employee_id = e.id
             where upper(trim(e.rule_system_code)) = :ruleSystemCode
               and upper(trim(e.employee_type_code)) = :employeeTypeCode
               and trim(e.employee_number) = :employeeNumber
               and wt.start_date <= :periodEnd
               and (wt.end_date is null or wt.end_date >= :periodStart)
             order by wt.start_date asc, wt.working_time_number asc
            """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .setParameter("periodStart", periodStart)
                .setParameter("periodEnd", periodEnd)
                .getResultList();

        List<PayrollLaunchWorkingTimeWindowContext> windows = workingTimeRows.stream()
                .map(this::toWorkingTimeWindow)
                .toList();

        return Optional.of(new PayrollLaunchEligibleInputContext(companyCode, agreementCode, windows));
    }

    private PayrollLaunchWorkingTimeWindowContext toWorkingTimeWindow(Object row) {
        if (!(row instanceof Object[] columns) || columns.length < 3) {
            throw new IllegalStateException("Unexpected row shape for payroll launch eligible input working-time query");
        }
        return new PayrollLaunchWorkingTimeWindowContext(
                toLocalDate(columns[0]),
                toLocalDate(columns[1]),
                (BigDecimal) columns[2]
        );
    }

    private LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return (LocalDate) value;
    }
}
