package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierNotFoundException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteIdentifierServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private IdentifierRepository identifierRepository;
    @Mock
    private EmployeeIdentifierLookupPort employeeIdentifierLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private DeleteIdentifierService service;

    @BeforeEach
    void setUp() {
        service = new DeleteIdentifierService(identifierRepository, employeeIdentifierLookupPort, ruleSystemRepository);
    }

    @Test
    void deletesIdentifierByBusinessKey() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.of(existingIdentifier(10L, "NATIONAL_ID")));

        service.delete(new DeleteIdentifierCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "national_id"));

        verify(identifierRepository).deleteByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID");
    }

    @Test
    void rejectsDeleteWhenIdentifierDoesNotExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID")).thenReturn(Optional.empty());

        assertThrows(
                IdentifierNotFoundException.class,
                () -> service.delete(new DeleteIdentifierCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "NATIONAL_ID"))
        );
        verify(identifierRepository, never()).deleteByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID");
    }

    @Test
    void rejectsDeleteWhenEmployeeBusinessKeyDoesNotExist() {
        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                IdentifierEmployeeNotFoundException.class,
                () -> service.delete(new DeleteIdentifierCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER, "NATIONAL_ID"))
        );
        verify(identifierRepository, never()).deleteByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID");
    }

    private EmployeeIdentifierContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeIdentifierContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
    }

    private Identifier existingIdentifier(Long employeeId, String identifierTypeCode) {
        return new Identifier(
                20L,
                employeeId,
                identifierTypeCode,
                "12345678A",
                null,
                null,
                false,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private RuleSystem ruleSystem(String code) {
        return new RuleSystem(
                1L,
                code,
                "Spain",
                "ESP",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
