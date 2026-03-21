package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.exception.RuleSystemNotFoundException;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateRuleSystemServiceTest {

    @Mock
    private RuleSystemRepository ruleSystemRepository;

    private UpdateRuleSystemService service;

    @BeforeEach
    void setUp() {
        service = new UpdateRuleSystemService(ruleSystemRepository);
    }

    @Test
    void updatesRuleSystemWithoutChangingBusinessKeyAndPersistsChanges() {
        RuleSystem existing = ruleSystem(1L, "ESP", "Spain", "ESP", true);
        RuleSystem saved = ruleSystem(1L, "ESP", "Spain Updated", "FRA", false);

        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(existing));
        when(ruleSystemRepository.save(existing)).thenReturn(saved);

        RuleSystem result = service.execute(new UpdateRuleSystemCommand(" esp ", " Spain Updated ", " fra ", false));

        ArgumentCaptor<RuleSystem> captor = ArgumentCaptor.forClass(RuleSystem.class);
        verify(ruleSystemRepository).save(captor.capture());
        RuleSystem persisted = captor.getValue();

        assertEquals("ESP", persisted.getCode());
        assertEquals("Spain Updated", persisted.getName());
        assertEquals("FRA", persisted.getCountryCode());
        assertEquals(false, persisted.isActive());

        assertEquals("ESP", result.getCode());
        assertEquals("Spain Updated", result.getName());
        assertEquals("FRA", result.getCountryCode());
        assertEquals(false, result.isActive());
    }

    @Test
    void throwsNotFoundWhenRuleSystemDoesNotExist() {
        when(ruleSystemRepository.findByCode("ESP")).thenReturn(Optional.empty());

        assertThrows(
                RuleSystemNotFoundException.class,
                () -> service.execute(new UpdateRuleSystemCommand("ESP", "Spain Updated", "ESP", true))
        );
    }

    private RuleSystem ruleSystem(Long id, String code, String name, String countryCode, boolean active) {
        return new RuleSystem(
                id,
                code,
                name,
                countryCode,
                active,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
