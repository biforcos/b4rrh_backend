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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRuleSystemByCodeServiceTest {

    @Mock private RuleSystemRepository ruleSystemRepository;

    private GetRuleSystemByCodeService service;

    @BeforeEach
    void setUp() {
        service = new GetRuleSystemByCodeService(ruleSystemRepository);
    }

    @Test
    void returnsRuleSystemWhenFound() {
        RuleSystem ruleSystem = new RuleSystem(1L, "ESP", "Spain", "ES", true, null, null);
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(ruleSystem));

        Optional<RuleSystem> result = service.getByCode("ESP");

        assertTrue(result.isPresent());
        assertEquals("ESP", result.get().getCode());
    }

    @Test
    void returnsEmptyWhenNotFound() {
        when(ruleSystemRepository.findByCode("UNKNOWN")).thenReturn(Optional.empty());

        Optional<RuleSystem> result = service.getByCode("UNKNOWN");

        assertTrue(result.isEmpty());
    }

    @Test
    void normalizesCodeBeforeLookup() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.empty());

        service.getByCode(" esp ");

        verify(ruleSystemRepository).findByCode("ESP");
    }
}
