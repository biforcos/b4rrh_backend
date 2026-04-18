package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRuleSystemsServiceTest {

    @Mock private RuleSystemRepository ruleSystemRepository;

    private ListRuleSystemsService service;

    @BeforeEach
    void setUp() {
        service = new ListRuleSystemsService(ruleSystemRepository);
    }

    @Test
    void returnsAllRuleSystems() {
        List<RuleSystem> systems = List.of(
                new RuleSystem(1L, "ESP", "Spain", "ES", true, null, null),
                new RuleSystem(2L, "PRT", "Portugal", "PT", true, null, null)
        );
        when(ruleSystemRepository.findAll()).thenReturn(systems);

        List<RuleSystem> result = service.listAll();

        assertEquals(2, result.size());
        assertEquals("ESP", result.get(0).getCode());
        assertEquals("PRT", result.get(1).getCode());
    }

    @Test
    void returnsEmptyListWhenNoSystems() {
        when(ruleSystemRepository.findAll()).thenReturn(List.of());

        List<RuleSystem> result = service.listAll();

        assertEquals(0, result.size());
    }
}
