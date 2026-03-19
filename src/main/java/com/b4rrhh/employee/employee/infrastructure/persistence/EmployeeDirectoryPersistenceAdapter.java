package com.b4rrhh.employee.employee.infrastructure.persistence;

import com.b4rrhh.employee.employee.domain.model.EmployeeDirectoryItem;
import com.b4rrhh.employee.employee.domain.port.EmployeeDirectoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Component
public class EmployeeDirectoryPersistenceAdapter implements EmployeeDirectoryRepository {

    private final SpringDataEmployeeRepository springDataEmployeeRepository;

    public EmployeeDirectoryPersistenceAdapter(SpringDataEmployeeRepository springDataEmployeeRepository) {
        this.springDataEmployeeRepository = springDataEmployeeRepository;
    }

    @Override
    public List<EmployeeDirectoryItem> findDirectoryByFilters(
            String queryText,
            String ruleSystemCode,
            String employeeTypeCode,
            String status,
            int page,
            int size
    ) {
        return springDataEmployeeRepository
                .findDirectoryByFilters(
                        queryText,
                        ruleSystemCode,
                        employeeTypeCode,
                        status,
                        LocalDate.now(),
                        PageRequest.of(page, size)
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private EmployeeDirectoryItem toDomain(EmployeeDirectoryProjection projection) {
        return new EmployeeDirectoryItem(
                projection.ruleSystemCode(),
                projection.employeeTypeCode(),
                projection.employeeNumber(),
                buildDisplayName(projection),
                projection.status(),
                projection.workCenterCode()
        );
    }

    private String buildDisplayName(EmployeeDirectoryProjection projection) {
        String preferredName = normalizeNamePart(projection.preferredName());
        if (preferredName != null) {
            return preferredName;
        }

        String fullName = Stream.of(
                        normalizeNamePart(projection.firstName()),
                        normalizeNamePart(projection.lastName1()),
                        normalizeNamePart(projection.lastName2())
                )
                .filter(part -> part != null && !part.isEmpty())
                .reduce((left, right) -> left + " " + right)
                .orElse("")
                .trim();

        if (!fullName.isEmpty()) {
            return fullName;
        }

        return projection.employeeNumber();
    }

    private String normalizeNamePart(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}