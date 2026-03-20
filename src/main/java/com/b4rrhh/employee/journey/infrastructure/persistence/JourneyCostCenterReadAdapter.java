package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.JourneyCostCenterReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyCostCenterRecord;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toBigDecimal;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toLocalDate;

@Component
public class JourneyCostCenterReadAdapter implements JourneyCostCenterReadPort {

    private final EntityManager entityManager;

    public JourneyCostCenterReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<JourneyCostCenterRecord> findByEmployeeIdOrderByStartDate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select cost_center_code,
                       allocation_percentage,
                       start_date,
                       end_date
                from employee.cost_center
                where employee_id = :employeeId
                order by start_date asc, cost_center_code asc
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return rows.stream()
                .map(this::toCostCenterRecord)
                .toList();
    }

    private JourneyCostCenterRecord toCostCenterRecord(Object row) {
        Object[] columns = requireColumns(row, 4, "journey cost center query");

        return new JourneyCostCenterRecord(
                (String) columns[0],
                toBigDecimal(columns[1]),
                toLocalDate(columns[2]),
                toLocalDate(columns[3])
        );
    }
}
