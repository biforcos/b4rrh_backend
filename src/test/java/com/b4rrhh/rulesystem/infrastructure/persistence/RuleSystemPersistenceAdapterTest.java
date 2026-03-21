package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    void preservesCreatedAtAndUpdatedAtWhenSavingExistingRuleSystem() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 8, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 20, 9, 30, 0);

        RuleSystem existingRuleSystem = new RuleSystem(
                1L,
                "ESP",
                "Sistema de Reglas Espana",
                "ESP",
                true,
                createdAt,
                updatedAt
        );

        when(springDataRuleSystemRepository.save(any(RuleSystemEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RuleSystem saved = adapter.save(existingRuleSystem);

        ArgumentCaptor<RuleSystemEntity> entityCaptor = ArgumentCaptor.forClass(RuleSystemEntity.class);
        verify(springDataRuleSystemRepository).save(entityCaptor.capture());

        RuleSystemEntity persistedEntity = entityCaptor.getValue();
        assertEquals(createdAt, persistedEntity.getCreatedAt());
        assertEquals(updatedAt, persistedEntity.getUpdatedAt());

        assertEquals(createdAt, saved.getCreatedAt());
        assertEquals(updatedAt, saved.getUpdatedAt());
    }
}
