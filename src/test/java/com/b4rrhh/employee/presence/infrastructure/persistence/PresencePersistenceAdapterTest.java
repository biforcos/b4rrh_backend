package com.b4rrhh.employee.presence.infrastructure.persistence;

import com.b4rrhh.employee.presence.domain.model.Presence;
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
class PresencePersistenceAdapterTest {

    @Mock
    private SpringDataPresenceRepository springDataPresenceRepository;

    private PresencePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PresencePersistenceAdapter(springDataPresenceRepository);
    }

    @Test
    void preservesCreatedAtAndUpdatedAtWhenSavingExistingPresence() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 10, 8, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 20, 9, 30, 0);

        Presence existingPresence = new Presence(
                20L,
                10L,
                1,
                "AC01",
                "ENT01",
                "TERMINATION",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 1),
                createdAt,
                updatedAt
        );

        when(springDataPresenceRepository.save(any(PresenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Presence saved = adapter.save(existingPresence);

        ArgumentCaptor<PresenceEntity> entityCaptor = ArgumentCaptor.forClass(PresenceEntity.class);
        verify(springDataPresenceRepository).save(entityCaptor.capture());

        PresenceEntity persistedEntity = entityCaptor.getValue();
        assertEquals(createdAt, persistedEntity.getCreatedAt());
        assertEquals(updatedAt, persistedEntity.getUpdatedAt());

        assertEquals(createdAt, saved.getCreatedAt());
        assertEquals(updatedAt, saved.getUpdatedAt());
    }
}
