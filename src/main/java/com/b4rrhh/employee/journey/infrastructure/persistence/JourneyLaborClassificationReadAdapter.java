package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationRecord;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toLocalDate;

@Component
public class JourneyLaborClassificationReadAdapter implements JourneyLaborClassificationReadPort {

    private final EntityManager entityManager;

    public JourneyLaborClassificationReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<JourneyLaborClassificationRecord> findByEmployeeIdOrderByStartDate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select agreement_code,
                       agreement_category_code,
                       start_date,
                       end_date
                from employee.labor_classification
                where employee_id = :employeeId
                order by start_date asc
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return rows.stream()
                .map(this::toLaborClassificationRecord)
                .toList();
    }

    private JourneyLaborClassificationRecord toLaborClassificationRecord(Object row) {
        Object[] columns = requireColumns(row, 4, "journey labor classification query");

        return new JourneyLaborClassificationRecord(
                (String) columns[0],
                (String) columns[1],
                toLocalDate(columns[2]),
                toLocalDate(columns[3])
        );
    }
}
