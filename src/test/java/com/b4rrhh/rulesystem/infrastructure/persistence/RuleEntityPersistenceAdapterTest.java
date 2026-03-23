package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEntityPersistenceAdapterTest {

    @Mock
    private SpringDataRuleEntityRepository springDataRuleEntityRepository;

    private RuleEntityPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RuleEntityPersistenceAdapter(springDataRuleEntityRepository);
    }

    @Test
    void createFlowPersistsNewEntityWithoutLoadingExistingOccurrence() {
        RuleEntity newRuleEntity = new RuleEntity(
                null,
                "ESP",
                "WORK_CENTER",
                "MADRID",
                "Madrid",
                "Initial description",
                true,
                LocalDate.of(2026, 1, 1),
                null,
                null,
                null
        );

        when(springDataRuleEntityRepository.save(any(RuleEntityEntity.class)))
                .thenAnswer(invocation -> {
                    RuleEntityEntity entity = invocation.getArgument(0);
                    entity.setId(90L);
                    entity.setCreatedAt(LocalDateTime.of(2026, 1, 10, 8, 0, 0));
                    entity.setUpdatedAt(LocalDateTime.of(2026, 1, 10, 8, 0, 0));
                    return entity;
                });

        RuleEntity saved = adapter.save(newRuleEntity);

        verify(springDataRuleEntityRepository, never())
                .findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(any(), any(), any(), any());

        ArgumentCaptor<RuleEntityEntity> entityCaptor = ArgumentCaptor.forClass(RuleEntityEntity.class);
        verify(springDataRuleEntityRepository).save(entityCaptor.capture());

        RuleEntityEntity persisted = entityCaptor.getValue();
        assertEquals("ESP", persisted.getRuleSystemCode());
        assertEquals("WORK_CENTER", persisted.getRuleEntityTypeCode());
        assertEquals("MADRID", persisted.getCode());
        assertEquals(LocalDate.of(2026, 1, 1), persisted.getStartDate());
        assertEquals("Madrid", persisted.getName());
        assertEquals("Initial description", persisted.getDescription());
        assertEquals(true, persisted.isActive());
        assertEquals(null, persisted.getEndDate());

        assertNotNull(saved.getId());
        assertEquals("ESP", saved.getRuleSystemCode());
        assertEquals("WORK_CENTER", saved.getRuleEntityTypeCode());
        assertEquals("MADRID", saved.getCode());
        assertEquals(LocalDate.of(2026, 1, 1), saved.getStartDate());
    }

    @Test
    void updateFlowLoadsExistingEntityAndPreservesCreatedAt() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 5, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 20, 15, 30, 0);

        RuleEntity updatedDomain = new RuleEntity(
                10L,
                "ESP",
                "WORK_CENTER",
                "MADRID",
                "Madrid Updated",
                "Updated description",
                false,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                createdAt,
                updatedAt
        );

        RuleEntityEntity existing = new RuleEntityEntity();
        existing.setId(10L);
        existing.setRuleSystemCode("ESP");
        existing.setRuleEntityTypeCode("WORK_CENTER");
        existing.setCode("MADRID");
        existing.setStartDate(LocalDate.of(2026, 1, 1));
        existing.setName("Madrid");
        existing.setDescription("Original description");
        existing.setActive(true);
        existing.setEndDate(null);
        existing.setCreatedAt(createdAt);
        existing.setUpdatedAt(updatedAt);

        when(springDataRuleEntityRepository.findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1)
        )).thenReturn(Optional.of(existing));

        when(springDataRuleEntityRepository.save(any(RuleEntityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RuleEntity saved = adapter.save(updatedDomain);

        ArgumentCaptor<RuleEntityEntity> entityCaptor = ArgumentCaptor.forClass(RuleEntityEntity.class);
        verify(springDataRuleEntityRepository).save(entityCaptor.capture());

        RuleEntityEntity persisted = entityCaptor.getValue();
        assertSame(existing, persisted);
        assertEquals(createdAt, persisted.getCreatedAt());
        assertEquals(updatedAt, persisted.getUpdatedAt());
        assertEquals("Madrid Updated", persisted.getName());
        assertEquals("Updated description", persisted.getDescription());
        assertEquals(false, persisted.isActive());
        assertEquals(LocalDate.of(2026, 12, 31), persisted.getEndDate());

        assertEquals(createdAt, saved.getCreatedAt());
        assertEquals(updatedAt, saved.getUpdatedAt());
    }

    @Test
    void updateFlowReturnsSavedCanonicalOccurrence() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 5, 10, 0, 0);

        RuleEntity updatedDomain = new RuleEntity(
                10L,
                "ESP",
                "WORK_CENTER",
                "MADRID",
                "Madrid Updated",
                "Updated description",
                false,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                createdAt,
                null
        );

        RuleEntityEntity existing = new RuleEntityEntity();
        existing.setId(10L);
        existing.setRuleSystemCode("ESP");
        existing.setRuleEntityTypeCode("WORK_CENTER");
        existing.setCode("MADRID");
        existing.setStartDate(LocalDate.of(2026, 1, 1));
        existing.setName("Madrid");
        existing.setDescription("Original description");
        existing.setActive(true);
        existing.setEndDate(null);
        existing.setCreatedAt(createdAt);
        existing.setUpdatedAt(LocalDateTime.of(2026, 1, 20, 15, 30, 0));

        when(springDataRuleEntityRepository.findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1)
        )).thenReturn(Optional.of(existing));

        when(springDataRuleEntityRepository.save(any(RuleEntityEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RuleEntity saved = adapter.save(updatedDomain);

        assertEquals(10L, saved.getId());
        assertEquals("ESP", saved.getRuleSystemCode());
        assertEquals("WORK_CENTER", saved.getRuleEntityTypeCode());
        assertEquals("MADRID", saved.getCode());
        assertEquals(LocalDate.of(2026, 1, 1), saved.getStartDate());
        assertEquals("Madrid Updated", saved.getName());
        assertEquals("Updated description", saved.getDescription());
        assertEquals(false, saved.isActive());
        assertEquals(LocalDate.of(2026, 12, 31), saved.getEndDate());
    }

    @Test
    void updateFlowFailsLoudlyWhenOccurrenceDoesNotExist() {
        RuleEntity updatedDomain = new RuleEntity(
                10L,
                "ESP",
                "WORK_CENTER",
                "MADRID",
                "Madrid Updated",
                "Updated description",
                false,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null
        );

        when(springDataRuleEntityRepository.findByRuleSystemCodeAndRuleEntityTypeCodeAndCodeAndStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1)
        )).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> adapter.save(updatedDomain));

        assertEquals(
                "Rule entity not found for update with business key: ESP/WORK_CENTER/MADRID/2026-01-01",
                exception.getMessage()
        );
    }

    @Test
    void overlapCheckUsesMaxDateWhenProjectedEndDateIsNull() {
        when(springDataRuleEntityRepository.existsOverlapExcludingStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1),
                SpringDataRuleEntityRepository.MAX_DATE,
                LocalDate.of(2026, 1, 1),
                SpringDataRuleEntityRepository.MAX_DATE
        )).thenReturn(false);

        boolean overlap = adapter.existsOverlapExcludingStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1),
                null,
                LocalDate.of(2026, 1, 1)
        );

        assertEquals(false, overlap);
        verify(springDataRuleEntityRepository).existsOverlapExcludingStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1),
                SpringDataRuleEntityRepository.MAX_DATE,
                LocalDate.of(2026, 1, 1),
                SpringDataRuleEntityRepository.MAX_DATE
        );
    }

    @Test
    void overlapCheckUsesProjectedEndDateWhenProvided() {
        LocalDate projectedEndDate = LocalDate.of(2026, 12, 31);
        when(springDataRuleEntityRepository.existsOverlapExcludingStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1),
                projectedEndDate,
                LocalDate.of(2026, 1, 1),
                SpringDataRuleEntityRepository.MAX_DATE
        )).thenReturn(true);

        boolean overlap = adapter.existsOverlapExcludingStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1),
                projectedEndDate,
                LocalDate.of(2026, 1, 1)
        );

        assertEquals(true, overlap);
        verify(springDataRuleEntityRepository).existsOverlapExcludingStartDate(
                "ESP",
                "WORK_CENTER",
                "MADRID",
                LocalDate.of(2026, 1, 1),
                projectedEndDate,
                LocalDate.of(2026, 1, 1),
                SpringDataRuleEntityRepository.MAX_DATE
        );
    }
}
