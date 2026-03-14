package com.b4rrhh.employee.identifier.application.usecase;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.identifier.application.service.IdentifierCatalogValidator;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierEmployeeNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierCatalogValueInvalidException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierNotFoundException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierPrimaryAlreadyExistsException;
import com.b4rrhh.employee.identifier.domain.exception.IdentifierValueInvalidException;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.domain.port.IdentifierRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateIdentifierServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private IdentifierRepository identifierRepository;
    @Mock
    private EmployeeIdentifierLookupPort employeeIdentifierLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private UpdateIdentifierService service;

    @BeforeEach
    void setUp() {
        IdentifierCatalogValidator identifierCatalogValidator = new IdentifierCatalogValidator(ruleEntityRepository);
        service = new UpdateIdentifierService(
                identifierRepository,
                employeeIdentifierLookupPort,
                ruleSystemRepository,
                identifierCatalogValidator
        );
    }

    @Test
    void updatesIdentifierByBusinessKey() {
        UpdateIdentifierCommand command = new UpdateIdentifierCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "national_id",
                "87654321Z",
                "esp",
                LocalDate.of(2035, 1, 1),
                true
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.of(existingIdentifier(10L, "NATIONAL_ID", false)));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "EMPLOYEE_IDENTIFIER_TYPE", "NATIONAL_ID"))
                .thenReturn(Optional.of(activeIdentifierTypeRuleEntity(RULE_SYSTEM_CODE, "NATIONAL_ID")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "COUNTRY", "ESP"))
                .thenReturn(Optional.of(activeCountryRuleEntity(RULE_SYSTEM_CODE, "ESP")));
        when(identifierRepository.existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(10L, "NATIONAL_ID"))
                .thenReturn(false);
        when(identifierRepository.save(any(Identifier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Identifier updated = service.update(command);

        assertEquals("NATIONAL_ID", updated.getIdentifierTypeCode());
        assertEquals("87654321Z", updated.getIdentifierValue());
        assertEquals("ESP", updated.getIssuingCountryCode());
        assertEquals(true, updated.isPrimary());
    }

    @Test
    void rejectsUpdateWhenIdentifierDoesNotExist() {
        UpdateIdentifierCommand command = new UpdateIdentifierCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "NATIONAL_ID",
                "87654321Z",
                null,
                null,
                false
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID")).thenReturn(Optional.empty());

        assertThrows(IdentifierNotFoundException.class, () -> service.update(command));
    }

    @Test
    void rejectsUpdateWhenPrimaryAlreadyExistsForAnotherType() {
        UpdateIdentifierCommand command = new UpdateIdentifierCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "NATIONAL_ID",
                "87654321Z",
                null,
                null,
                true
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.of(existingIdentifier(10L, "NATIONAL_ID", false)));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "EMPLOYEE_IDENTIFIER_TYPE", "NATIONAL_ID"))
                .thenReturn(Optional.of(activeIdentifierTypeRuleEntity(RULE_SYSTEM_CODE, "NATIONAL_ID")));
        when(identifierRepository.existsByEmployeeIdAndIsPrimaryTrueAndIdentifierTypeCodeNot(10L, "NATIONAL_ID"))
                .thenReturn(true);

        assertThrows(IdentifierPrimaryAlreadyExistsException.class, () -> service.update(command));
    }

    @Test
    void rejectsUpdateWhenIssuingCountryCodeIsInvalid() {
        UpdateIdentifierCommand command = new UpdateIdentifierCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "NATIONAL_ID",
                "87654321Z",
                "ZZZ",
                null,
                false
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.of(existingIdentifier(10L, "NATIONAL_ID", false)));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "EMPLOYEE_IDENTIFIER_TYPE", "NATIONAL_ID"))
                .thenReturn(Optional.of(activeIdentifierTypeRuleEntity(RULE_SYSTEM_CODE, "NATIONAL_ID")));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "COUNTRY", "ZZZ"))
                .thenReturn(Optional.empty());

        assertThrows(IdentifierCatalogValueInvalidException.class, () -> service.update(command));
    }

    @Test
    void rejectsUpdateWhenIdentifierValueIsInvalid() {
        UpdateIdentifierCommand command = new UpdateIdentifierCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "NATIONAL_ID",
                "  ",
                null,
                null,
                false
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L, EMPLOYEE_NUMBER)));
        when(identifierRepository.findByEmployeeIdAndIdentifierTypeCode(10L, "NATIONAL_ID"))
                .thenReturn(Optional.of(existingIdentifier(10L, "NATIONAL_ID", false)));
        when(ruleEntityRepository.findByBusinessKey(RULE_SYSTEM_CODE, "EMPLOYEE_IDENTIFIER_TYPE", "NATIONAL_ID"))
                .thenReturn(Optional.of(activeIdentifierTypeRuleEntity(RULE_SYSTEM_CODE, "NATIONAL_ID")));

        assertThrows(IdentifierValueInvalidException.class, () -> service.update(command));
    }

    @Test
    void rejectsUpdateWhenEmployeeBusinessKeyDoesNotExist() {
        UpdateIdentifierCommand command = new UpdateIdentifierCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "NATIONAL_ID",
                "87654321Z",
                null,
                null,
                false
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeIdentifierLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(IdentifierEmployeeNotFoundException.class, () -> service.update(command));
    }

    private EmployeeIdentifierContext employeeContext(Long employeeId, String employeeNumber) {
        return new EmployeeIdentifierContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, employeeNumber);
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

    private RuleEntity activeIdentifierTypeRuleEntity(String ruleSystemCode, String code) {
        return new RuleEntity(
                1L,
                ruleSystemCode,
                "EMPLOYEE_IDENTIFIER_TYPE",
                code,
                code,
                null,
                true,
                LocalDate.of(1900, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

        private RuleEntity activeCountryRuleEntity(String ruleSystemCode, String code) {
                return new RuleEntity(
                                2L,
                                ruleSystemCode,
                                "COUNTRY",
                                code,
                                code,
                                "Country",
                                true,
                                LocalDate.of(1900, 1, 1),
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                );
        }

    private Identifier existingIdentifier(Long employeeId, String identifierTypeCode, boolean isPrimary) {
        return new Identifier(
                20L,
                employeeId,
                identifierTypeCode,
                "EXISTING",
                null,
                null,
                isPrimary,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
