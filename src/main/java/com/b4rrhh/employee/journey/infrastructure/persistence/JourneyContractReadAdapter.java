package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.JourneyContractReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractRecord;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;
import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.toLocalDate;

@Component
public class JourneyContractReadAdapter implements JourneyContractReadPort {

    private final EntityManager entityManager;

    public JourneyContractReadAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<JourneyContractRecord> findByEmployeeIdOrderByStartDate(Long employeeId) {
        List<?> rows = entityManager.createNativeQuery("""
                select contract_code,
                       contract_subtype_code,
                       start_date,
                       end_date
                from employee.contract
                where employee_id = :employeeId
                order by start_date asc
                """)
                .setParameter("employeeId", employeeId)
                .getResultList();

        return rows.stream()
                .map(this::toContractRecord)
                .toList();
    }

    private JourneyContractRecord toContractRecord(Object row) {
        Object[] columns = requireColumns(row, 4, "journey contract query");

        return new JourneyContractRecord(
                (String) columns[0],
                (String) columns[1],
                toLocalDate(columns[2]),
                toLocalDate(columns[3])
        );
    }
}
