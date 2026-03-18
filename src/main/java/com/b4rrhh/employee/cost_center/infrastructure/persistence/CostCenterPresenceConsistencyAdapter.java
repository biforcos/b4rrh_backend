package com.b4rrhh.employee.cost_center.infrastructure.persistence;

import com.b4rrhh.employee.cost_center.application.port.CostCenterPresenceConsistencyPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CostCenterPresenceConsistencyAdapter implements CostCenterPresenceConsistencyPort {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final EntityManager entityManager;

    public CostCenterPresenceConsistencyAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existsPresenceContainingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveEndDate = endDate == null ? MAX_DATE : endDate;

        Object result = entityManager.createNativeQuery("""
                select case when count(p) > 0 then true else false end
                from employee.presence p
                where p.employee_id = :employeeId
                  and p.start_date <= :startDate
                  and :effectiveEndDate <= coalesce(p.end_date, :maxDate)
                """)
                .setParameter("employeeId", employeeId)
                .setParameter("startDate", startDate)
                .setParameter("effectiveEndDate", effectiveEndDate)
                .setParameter("maxDate", MAX_DATE)
                .getSingleResult();

        if (result instanceof Boolean boolResult) {
            return boolResult;
        }
        if (result instanceof Number numberResult) {
            return numberResult.intValue() > 0;
        }

        return Boolean.parseBoolean(String.valueOf(result));
    }
}
