package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertEmployeeNumberingConfigServiceTest {

    @Mock
    private EmployeeNumberingConfigRepository configRepository;
    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private UpsertEmployeeNumberingConfigService service;

    @BeforeEach
    void setUp() {
        service = new UpsertEmployeeNumberingConfigService(configRepository, ruleSystemRepository);
    }

    @Test
    void savesValidConfig() {
        when(ruleSystemRepository.findByCode("ESP"))
                .thenReturn(Optional.of(new RuleSystem(1L, "ESP", "Spain", "ES", true, null, null)));
        when(configRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpsertEmployeeNumberingConfigCommand command =
                new UpsertEmployeeNumberingConfigCommand("ESP", "EMP", 6, 1, 1L);

        EmployeeNumberingConfig result = service.upsert(command);

        assertEquals("ESP", result.ruleSystemCode());
        assertEquals("EMP", result.prefix());
        assertEquals(6, result.numericPartLength());
        verify(configRepository).save(any());
    }

    @Test
    void rejectsWhenPrefixPlusLengthExceedsFifteen() {
        when(ruleSystemRepository.findByCode("ESP"))
                .thenReturn(Optional.of(new RuleSystem(1L, "ESP", "Spain", "ES", true, null, null)));
        // prefix "EMP" (3) + numericPartLength 13 = 16 > 15
        UpsertEmployeeNumberingConfigCommand command =
                new UpsertEmployeeNumberingConfigCommand("ESP", "EMP", 13, 1, 1L);

        assertThrows(EmployeeNumberingConfigInvalidException.class, () -> service.upsert(command));
        verify(configRepository, never()).save(any());
    }

    @Test
    void rejectsWhenRuleSystemDoesNotExist() {
        when(ruleSystemRepository.findByCode("NOPE")).thenReturn(Optional.empty());
        UpsertEmployeeNumberingConfigCommand command =
                new UpsertEmployeeNumberingConfigCommand("NOPE", "", 8, 1, 1L);

        assertThrows(IllegalArgumentException.class, () -> service.upsert(command));
        verify(configRepository, never()).save(any());
    }
}
