package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleSystemPersistenceAdapterTest {

    @Mock
    private SpringDataRuleSystemRepository springDataRuleSystemRepository;

    private RuleSystemPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RuleSystemPersistenceAdapter(springDataRuleSystemRepository);
    }

    @Test
    void createsRuleSystemWithoutLoadingExistingEntity() {
        RuleSystem newRuleSystem = new RuleSystem(
                null,
                "ESP",
                "Sistema de Reglas Espana",
                "ESP",
                true,
                null,
                null
        );

        when(springDataRuleSystemRepository.save(any(RuleSystemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RuleSystem saved = adapter.save(newRuleSystem);

        verify(springDataRuleSystemRepository, never()).findByCode(any(String.class));

        ArgumentCaptor<RuleSystemEntity> entityCaptor = ArgumentCaptor.forClass(RuleSystemEntity.class);
        verify(springDataRuleSystemRepository).save(entityCaptor.capture());

        RuleSystemEntity persistedEntity = entityCaptor.getValue();
        assertEquals("ESP", persistedEntity.getCode());
        assertEquals("Sistema de Reglas Espana", persistedEntity.getName());
        assertEquals("ESP", persistedEntity.getCountryCode());
        assertEquals(true, persistedEntity.isActive());

        assertEquals("ESP", saved.getCode());
        assertEquals("Sistema de Reglas Espana", saved.getName());
        assertEquals("ESP", saved.getCountryCode());
        assertEquals(true, saved.isActive());
    }

    @Test
    void updatesRuleSystemByLoadingExistingEntityAndMutatingIt() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 8, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 20, 9, 30, 0);

        RuleSystem existingRuleSystemDomain = new RuleSystem(
                1L,
                "ESP",
                "Sistema de Reglas Espana Actualizado",
                "ESP",
                false,
                createdAt,
                updatedAt
        );

        RuleSystemEntity existingEntity = new RuleSystemEntity();
        existingEntity.setId(1L);
        existingEntity.setCode("ESP");
        existingEntity.setName("Sistema de Reglas Espana");
        existingEntity.setCountryCode("ESP");
        existingEntity.setActive(true);
        existingEntity.setCreatedAt(createdAt);
        existingEntity.setUpdatedAt(updatedAt);

        when(springDataRuleSystemRepository.findByCode("ESP")).thenReturn(Optional.of(existingEntity));

        when(springDataRuleSystemRepository.save(any(RuleSystemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RuleSystem saved = adapter.save(existingRuleSystemDomain);

        ArgumentCaptor<RuleSystemEntity> entityCaptor = ArgumentCaptor.forClass(RuleSystemEntity.class);
        verify(springDataRuleSystemRepository).save(entityCaptor.capture());

        RuleSystemEntity persistedEntity = entityCaptor.getValue();
        assertSame(existingEntity, persistedEntity);
        assertEquals("ESP", persistedEntity.getCode());
        assertEquals("Sistema de Reglas Espana Actualizado", persistedEntity.getName());
        assertEquals("ESP", persistedEntity.getCountryCode());
        assertEquals(false, persistedEntity.isActive());
        assertEquals(createdAt, persistedEntity.getCreatedAt());
        assertEquals(updatedAt, persistedEntity.getUpdatedAt());

        assertEquals(createdAt, saved.getCreatedAt());
        assertEquals(updatedAt, saved.getUpdatedAt());
        assertEquals("Sistema de Reglas Espana Actualizado", saved.getName());
        assertEquals(false, saved.isActive());
    }
}
