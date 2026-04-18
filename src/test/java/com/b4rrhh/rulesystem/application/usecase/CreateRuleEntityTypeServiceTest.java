package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntityType;
import com.b4rrhh.rulesystem.domain.port.RuleEntityTypeRepository;
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
class CreateRuleEntityTypeServiceTest {

    @Mock private RuleEntityTypeRepository ruleEntityTypeRepository;

    private CreateRuleEntityTypeService service;

    @BeforeEach
    void setUp() {
        service = new CreateRuleEntityTypeService(ruleEntityTypeRepository);
    }

    @Test
    void createsTypeSuccessfully() {
        when(ruleEntityTypeRepository.findByCode("COMPANY")).thenReturn(Optional.empty());
        when(ruleEntityTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RuleEntityType result = service.create(new CreateRuleEntityTypeCommand("COMPANY", "Company"));

        assertEquals("COMPANY", result.getCode());
        assertEquals("Company", result.getName());
        assertTrue(result.isActive());
        verify(ruleEntityTypeRepository).save(any());
    }

    @Test
    void normalizesCodeToUpperCase() {
        when(ruleEntityTypeRepository.findByCode("COMPANY")).thenReturn(Optional.empty());
        when(ruleEntityTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RuleEntityType result = service.create(new CreateRuleEntityTypeCommand(" company ", "  Company  "));

        assertEquals("COMPANY", result.getCode());
        assertEquals("Company", result.getName());
    }

    @Test
    void failsWhenTypeWithSameCodeAlreadyExists() {
        when(ruleEntityTypeRepository.findByCode("COMPANY"))
                .thenReturn(Optional.of(new RuleEntityType(1L, "COMPANY", "Company", true, null, null)));

        assertThrows(IllegalArgumentException.class, () ->
                service.create(new CreateRuleEntityTypeCommand("COMPANY", "Company")));

        verify(ruleEntityTypeRepository, never()).save(any());
    }
}
