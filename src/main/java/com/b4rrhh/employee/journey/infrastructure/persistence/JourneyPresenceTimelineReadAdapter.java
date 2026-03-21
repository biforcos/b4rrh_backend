package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.JourneyPresenceTimelineReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceTimelineRecord;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toLocalDate;

@Component
public class JourneyPresenceTimelineReadAdapter implements JourneyPresenceTimelineReadPort {

    private final EntityManager entityManager;

    public JourneyPresenceTimelineReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<JourneyPresenceTimelineRecord> findByEmployeeIdOrderByStartDate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select presence_number,
                       company_code,
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

    private JourneyPresenceTimelineRecord toPresenceRecord(Object row) {
        Object[] columns = requireColumns(row, 6, "journey presence timeline query");

        return new JourneyPresenceTimelineRecord(
                toInteger(columns[0]),
                (String) columns[1],
                (String) columns[2],
                (String) columns[3],
                toLocalDate(columns[4]),
                toLocalDate(columns[5])
        );
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }

        throw new IllegalStateException("Unsupported integer value in journey presence timeline query: "
                + value.getClass().getName());
    }
}