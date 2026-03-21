package com.b4rrhh.employee.shared.infrastructure.persistence;

import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.employee.infrastructure.persistence.SpringDataEmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeOwnedLookupSupportTest {

    @Mock
        private SpringDataEmployeeRepository springDataEmployeeRepository;

    private EmployeeOwnedLookupSupport support;

    @BeforeEach
    void setUp() {
                EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport =
                                new EmployeeBusinessKeyLookupSupport(springDataEmployeeRepository);
        support = new EmployeeOwnedLookupSupport(employeeBusinessKeyLookupSupport);
    }

    @Test
    void findOwnedByBusinessKeyReturnsEmptyWhenEmployeeDoesNotExist() {
                when(springDataEmployeeRepository.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());

        Optional<String> result = support.findOwnedByBusinessKey(
                "ESP",
                "INTERNAL",
                "EMP001",
                employee -> Optional.of("owned")
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void findOwnedByBusinessKeyForUpdateResolvesOwnedResource() {
        EmployeeEntity employee = employeeEntity(12L, "ESP", "INTERNAL", "EMP001");
                when(springDataEmployeeRepository.findByBusinessKeyForUpdate("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee));

        Optional<String> result = support.findOwnedByBusinessKeyForUpdate(
                "ESP",
                "INTERNAL",
                "EMP001",
                e -> Optional.of("employee-" + e.getId())
        );

        assertTrue(result.isPresent());
        assertEquals("employee-12", result.get());
    }

    @Test
    void findOwnedByBusinessKeyOrThrowThrowsEmployeeNotFoundException() {
                when(springDataEmployeeRepository.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> support.findOwnedByBusinessKeyOrThrow(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        employee -> Optional.of("owned"),
                        () -> new IllegalArgumentException("employee missing"),
                        () -> new IllegalStateException("owned missing")
                )
        );

        assertEquals("employee missing", exception.getMessage());
    }

    @Test
    void findOwnedByBusinessKeyForUpdateOrThrowThrowsOwnedNotFoundException() {
        EmployeeEntity employee = employeeEntity(15L, "ESP", "INTERNAL", "EMP001");
                when(springDataEmployeeRepository.findByBusinessKeyForUpdate("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> support.findOwnedByBusinessKeyForUpdateOrThrow(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        e -> Optional.empty(),
                        () -> new IllegalArgumentException("employee missing"),
                        () -> new IllegalStateException("owned missing")
                )
        );

        assertEquals("owned missing", exception.getMessage());
    }

    private EmployeeEntity employeeEntity(Long id, String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setId(id);
        employee.setRuleSystemCode(ruleSystemCode);
        employee.setEmployeeTypeCode(employeeTypeCode);
        employee.setEmployeeNumber(employeeNumber);
        return employee;
    }
}