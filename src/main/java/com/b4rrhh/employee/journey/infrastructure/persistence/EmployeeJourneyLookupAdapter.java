package com.b4rrhh.employee.journey.infrastructure.persistence;

import com.b4rrhh.employee.journey.application.port.EmployeeJourneyLookupPort;
import com.b4rrhh.employee.journey.application.port.JourneyEmployeeContext;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.b4rrhh.employee.journey.infrastructure.persistence.JourneyRowMappingSupport.requireColumns;

@Component
public class EmployeeJourneyLookupAdapter implements EmployeeJourneyLookupPort {

    private final EntityManager entityManager;

    public EmployeeJourneyLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<JourneyEmployeeContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<?> rows = entityManager.createNativeQuery("""
                select id,
                       rule_system_code,
                       employee_type_code,
                       employee_number,
                       first_name,
                       last_name_1,
                       last_name_2,
                       preferred_name
                from employee.employee
                where upper(trim(rule_system_code)) = :ruleSystemCode
                  and upper(trim(employee_type_code)) = :employeeTypeCode
                  and trim(employee_number) = :employeeNumber
                """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("employeeTypeCode", employeeTypeCode)
                .setParameter("employeeNumber", employeeNumber)
                .getResultList();

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Object[] columns = requireColumns(rows.get(0), 8, "employee journey lookup query");

        Long employeeId = ((Number) columns[0]).longValue();
        String foundRuleSystemCode = (String) columns[1];
        String foundEmployeeTypeCode = (String) columns[2];
        String foundEmployeeNumber = (String) columns[3];
        String firstName = (String) columns[4];
        String lastName1 = (String) columns[5];
        String lastName2 = (String) columns[6];
        String preferredName = (String) columns[7];

        return Optional.of(new JourneyEmployeeContext(
                employeeId,
                foundRuleSystemCode,
                foundEmployeeTypeCode,
                foundEmployeeNumber,
                buildDisplayName(firstName, lastName1, lastName2, preferredName)
        ));
    }

    private String buildDisplayName(
            String firstName,
            String lastName1,
            String lastName2,
            String preferredName
    ) {
        if (preferredName != null && !preferredName.trim().isEmpty()) {
            return preferredName.trim();
        }

        StringBuilder fullName = new StringBuilder();
        appendNamePart(fullName, firstName);
        appendNamePart(fullName, lastName1);
        appendNamePart(fullName, lastName2);

        String normalized = fullName.toString().trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void appendNamePart(StringBuilder target, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (!target.isEmpty()) {
            target.append(' ');
        }
        target.append(value.trim());
    }
}
