package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
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
class CreateRuleSystemServiceTest {

    @Mock private RuleSystemRepository ruleSystemRepository;

    private CreateRuleSystemService service;

    @BeforeEach
    void setUp() {
        service = new CreateRuleSystemService(ruleSystemRepository);
    }

    @Test
    void createsRuleSystemSuccessfully() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.empty());
        when(ruleSystemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RuleSystem result = service.create(new CreateRuleSystemCommand("ESP", "Spain", "ES"));

        assertEquals("ESP", result.getCode());
        assertEquals("Spain", result.getName());
        assertEquals("ES", result.getCountryCode());
        assertTrue(result.isActive());
        verify(ruleSystemRepository).save(any());
    }

    @Test
    void normalizesCodeAndCountryCodeToUpperCase() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.empty());
        when(ruleSystemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RuleSystem result = service.create(new CreateRuleSystemCommand(" esp ", "  Spain  ", " es "));

        assertEquals("ESP", result.getCode());
        assertEquals("Spain", result.getName());
        assertEquals("ES", result.getCountryCode());
    }

    @Test
    void failsWhenRuleSystemWithSameCodeAlreadyExists() {
        when(ruleSystemRepository.findByCode("ESP"))
                .thenReturn(Optional.of(new RuleSystem(1L, "ESP", "Spain", "ES", true, null, null)));

        assertThrows(IllegalArgumentException.class, () ->
                service.create(new CreateRuleSystemCommand("ESP", "Spain", "ES")));

        verify(ruleSystemRepository, never()).save(any());
    }
}
