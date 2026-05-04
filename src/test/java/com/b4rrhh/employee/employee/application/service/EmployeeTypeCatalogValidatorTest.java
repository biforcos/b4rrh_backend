package com.b4rrhh.employee.employee.application.service;

import com.b4rrhh.employee.employee.domain.exception.EmployeeTypeInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeTypeCatalogValidatorTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private EmployeeTypeCatalogValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmployeeTypeCatalogValidator(ruleEntityRepository);
    }

    @Test
    void acceptsActiveCodeWithinDateRange() {
        RuleEntity entity = new RuleEntity(
                1L, "ESP", "EMPLOYEE_TYPE", "INTERNAL",
                "Internal Employee", null, true,
                LocalDate.of(1900, 1, 1), null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_TYPE", "INTERNAL"))
                .thenReturn(Optional.of(entity));

        assertDoesNotThrow(() ->
                validator.validateEmployeeTypeCode("ESP", "INTERNAL", LocalDate.of(2026, 1, 1)));
    }

    @Test
    void rejectsCodeNotInCatalog() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_TYPE", "BAD"))
                .thenReturn(Optional.empty());

        assertThrows(EmployeeTypeInvalidException.class, () ->
                validator.validateEmployeeTypeCode("ESP", "BAD", LocalDate.of(2026, 1, 1)));
    }

    @Test
    void rejectsInactiveCode() {
        RuleEntity entity = new RuleEntity(
                2L, "ESP", "EMPLOYEE_TYPE", "INTERNAL",
                "Internal Employee", null, false,
                LocalDate.of(1900, 1, 1), null,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_TYPE", "INTERNAL"))
                .thenReturn(Optional.of(entity));

        assertThrows(EmployeeTypeInvalidException.class, () ->
                validator.validateEmployeeTypeCode("ESP", "INTERNAL", LocalDate.of(2026, 1, 1)));
    }
}
