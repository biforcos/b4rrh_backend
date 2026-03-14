package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkCenterPersistenceAdapterTest {

    @Mock
    private SpringDataWorkCenterRepository springDataWorkCenterRepository;

    private WorkCenterPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new WorkCenterPersistenceAdapter(springDataWorkCenterRepository);
    }

    @Test
    void preservesCreatedAtAndUpdatedAtWhenSavingExistingWorkCenter() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 8, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 20, 9, 30, 0);

        WorkCenter existing = new WorkCenter(
                20L,
                10L,
                1,
                "MADRID_HQ",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 1),
                createdAt,
                updatedAt
        );

        when(springDataWorkCenterRepository.save(any(WorkCenterEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        WorkCenter saved = adapter.save(existing);

        ArgumentCaptor<WorkCenterEntity> captor = ArgumentCaptor.forClass(WorkCenterEntity.class);
        verify(springDataWorkCenterRepository).save(captor.capture());

        WorkCenterEntity persistedEntity = captor.getValue();
        assertEquals(createdAt, persistedEntity.getCreatedAt());
        assertEquals(updatedAt, persistedEntity.getUpdatedAt());

        assertEquals(createdAt, saved.getCreatedAt());
        assertEquals(updatedAt, saved.getUpdatedAt());
    }
}