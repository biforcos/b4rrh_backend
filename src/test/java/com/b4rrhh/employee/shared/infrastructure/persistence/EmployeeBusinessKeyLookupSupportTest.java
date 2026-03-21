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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeBusinessKeyLookupSupportTest {

    @Mock
    private SpringDataEmployeeRepository springDataEmployeeRepository;

    private EmployeeBusinessKeyLookupSupport support;

    @BeforeEach
    void setUp() {
        support = new EmployeeBusinessKeyLookupSupport(springDataEmployeeRepository);
    }

    @Test
    void delegatesBusinessKeyLookup() {
        EmployeeEntity employee = employeeEntity(10L, "ESP", "INTERNAL", "EMP001");
        when(springDataEmployeeRepository.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee));

        Optional<EmployeeEntity> result = support.findByBusinessKey("ESP", "INTERNAL", "EMP001");

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
    }

    @Test
    void delegatesBusinessKeyLookupForUpdate() {
        EmployeeEntity employee = employeeEntity(11L, "ESP", "EXTERNAL", "EMP002");
        when(springDataEmployeeRepository.findByBusinessKeyForUpdate("ESP", "EXTERNAL", "EMP002"))
                .thenReturn(Optional.of(employee));

        Optional<EmployeeEntity> result = support.findByBusinessKeyForUpdate("ESP", "EXTERNAL", "EMP002");

        assertTrue(result.isPresent());
        assertEquals(11L, result.get().getId());
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