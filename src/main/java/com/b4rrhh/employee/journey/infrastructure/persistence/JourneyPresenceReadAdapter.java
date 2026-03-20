package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.JourneyPresenceReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceRecord;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toLocalDate;

@Component
public class JourneyPresenceReadAdapter implements JourneyPresenceReadPort {

    private final EntityManager entityManager;

    public JourneyPresenceReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<JourneyPresenceRecord> findByEmployeeIdOrderByStartDate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select company_code,
                       entry_reason_code,
                       exit_reason_code,
                       start_date,
                       end_date
                from employee.presence
                where employee_id = :employeeId
                order by start_date asc, presence_number asc
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return rows.stream()
                .map(this::toPresenceRecord)
                .toList();
    }

    private JourneyPresenceRecord toPresenceRecord(Object row) {
        Object[] columns = requireColumns(row, 5, "journey presence query");

        return new JourneyPresenceRecord(
                (String) columns[0],
                (String) columns[1],
                (String) columns[2],
                toLocalDate(columns[3]),
                toLocalDate(columns[4])
        );
    }
}
