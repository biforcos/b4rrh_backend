package com.b4rrhh.employee.contact.infrastructure.persistence;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.employee.infrastructure.persistence.SpringDataEmployeeRepository;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeBusinessKeyLookupSupport;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeOwnedLookupSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeContactLookupAdapterTest {

    @Mock
    private SpringDataEmployeeRepository springDataEmployeeRepository;

    private EmployeeContactLookupAdapter adapter;

    @BeforeEach
    void setUp() {
        EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport =
                new EmployeeBusinessKeyLookupSupport(springDataEmployeeRepository);
        EmployeeOwnedLookupSupport employeeOwnedLookupSupport = new EmployeeOwnedLookupSupport(employeeBusinessKeyLookupSupport);
        adapter = new EmployeeContactLookupAdapter(employeeOwnedLookupSupport);
    }

    @Test
    void mapsEmployeeContextFromBusinessKeyLookup() {
        EmployeeEntity employee = employeeEntity(21L, "ESP", "INTERNAL", "EMP001");
        when(springDataEmployeeRepository.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee));

        Optional<EmployeeContactContext> result = adapter.findByBusinessKey("ESP", "INTERNAL", "EMP001");

        assertTrue(result.isPresent());
        assertEquals(21L, result.get().employeeId());
        assertEquals("ESP", result.get().ruleSystemCode());
        assertEquals("INTERNAL", result.get().employeeTypeCode());
        assertEquals("EMP001", result.get().employeeNumber());
    }

    @Test
    void mapsEmployeeContextFromBusinessKeyLookupForUpdate() {
        EmployeeEntity employee = employeeEntity(22L, "ESP", "EXTERNAL", "EMP002");
        when(springDataEmployeeRepository.findByBusinessKeyForUpdate("ESP", "EXTERNAL", "EMP002"))
                .thenReturn(Optional.of(employee));

        Optional<EmployeeContactContext> result = adapter.findByBusinessKeyForUpdate("ESP", "EXTERNAL", "EMP002");

        assertTrue(result.isPresent());
        assertEquals(22L, result.get().employeeId());
        assertEquals("ESP", result.get().ruleSystemCode());
        assertEquals("EXTERNAL", result.get().employeeTypeCode());
        assertEquals("EMP002", result.get().employeeNumber());
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