package com.b4rrhh.employee.address.infrastructure.persistence;

import com.b4rrhh.employee.address.application.port.EmployeeAddressContext;
import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
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
class EmployeeAddressLookupAdapterTest {

    @Mock
    private EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport;

    private EmployeeAddressLookupAdapter adapter;

    @BeforeEach
    void setUp() {
        EmployeeOwnedLookupSupport employeeOwnedLookupSupport = new EmployeeOwnedLookupSupport(employeeBusinessKeyLookupSupport);
        adapter = new EmployeeAddressLookupAdapter(employeeOwnedLookupSupport);
    }

    @Test
    void mapsEmployeeContextFromBusinessKeyLookup() {
        EmployeeEntity employee = employeeEntity(41L, "ESP", "INTERNAL", "EMP001");
        when(employeeBusinessKeyLookupSupport.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee));

        Optional<EmployeeAddressContext> result = adapter.findByBusinessKey("ESP", "INTERNAL", "EMP001");

        assertTrue(result.isPresent());
        assertEquals(41L, result.get().employeeId());
        assertEquals("ESP", result.get().ruleSystemCode());
        assertEquals("INTERNAL", result.get().employeeTypeCode());
        assertEquals("EMP001", result.get().employeeNumber());
    }

    @Test
    void mapsEmployeeContextFromBusinessKeyLookupForUpdate() {
        EmployeeEntity employee = employeeEntity(42L, "ESP", "EXTERNAL", "EMP002");
        when(employeeBusinessKeyLookupSupport.findByBusinessKeyForUpdate("ESP", "EXTERNAL", "EMP002"))
                .thenReturn(Optional.of(employee));

        Optional<EmployeeAddressContext> result = adapter.findByBusinessKeyForUpdate("ESP", "EXTERNAL", "EMP002");

        assertTrue(result.isPresent());
        assertEquals(42L, result.get().employeeId());
        assertEquals("ESP", result.get().ruleSystemCode());
        assertEquals("EXTERNAL", result.get().employeeTypeCode());
        assertEquals("EMP002", result.get().employeeNumber());
    }

    @Test
    void returnsEmptyWhenEmployeeDoesNotExist() {
        when(employeeBusinessKeyLookupSupport.findByBusinessKey("ESP", "INTERNAL", "EMP003"))
                .thenReturn(Optional.empty());

        Optional<EmployeeAddressContext> result = adapter.findByBusinessKey("ESP", "INTERNAL", "EMP003");

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenEmployeeDoesNotExistForUpdate() {
        when(employeeBusinessKeyLookupSupport.findByBusinessKeyForUpdate("ESP", "INTERNAL", "EMP004"))
                .thenReturn(Optional.empty());

        Optional<EmployeeAddressContext> result = adapter.findByBusinessKeyForUpdate("ESP", "INTERNAL", "EMP004");

        assertTrue(result.isEmpty());
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