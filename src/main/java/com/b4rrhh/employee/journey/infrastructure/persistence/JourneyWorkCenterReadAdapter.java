package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.JourneyWorkCenterReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyWorkCenterRecord;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toLocalDate;

@Component
public class JourneyWorkCenterReadAdapter implements JourneyWorkCenterReadPort {

    private final EntityManager entityManager;

    public JourneyWorkCenterReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<JourneyWorkCenterRecord> findByEmployeeIdOrderByStartDate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select work_center_code,
                       start_date,
                       end_date
                from employee.work_center
                where employee_id = :employeeId
                order by start_date asc, work_center_assignment_number asc
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return rows.stream()
                .map(this::toWorkCenterRecord)
                .toList();
    }

    private JourneyWorkCenterRecord toWorkCenterRecord(Object row) {
        Object[] columns = requireColumns(row, 3, "journey work center query");

        return new JourneyWorkCenterRecord(
                (String) columns[0],
                toLocalDate(columns[1]),
                toLocalDate(columns[2])
        );
    }
}
