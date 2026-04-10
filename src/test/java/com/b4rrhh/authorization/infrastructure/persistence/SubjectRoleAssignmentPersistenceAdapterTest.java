package com.b4rrhh.authorization.infrastructure.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubjectRoleAssignmentPersistenceAdapterTest {

    @Mock
    private SpringDataSubjectRoleAssignmentRepository springDataRepository;

    @InjectMocks
    private SubjectRoleAssignmentPersistenceAdapter adapter;

    @Test
    void returnsEmptyListWhenSubjectHasNoAssignments() {
        when(springDataRepository.findBySubjectCodeAndActiveTrue("bifor"))
                .thenReturn(List.of());

        assertThat(adapter.findActiveRoleCodesBySubjectCode("bifor")).isEmpty();
    }

    @Test
    void returnsRoleCodesForValidAssignmentOriginsWithoutRelyingOnOrder() {
        SubjectRoleAssignmentEntity adminAssignment = new SubjectRoleAssignmentEntity();
        adminAssignment.setSubjectCode("bifor");
        adminAssignment.setRoleCode("ADMIN");
        adminAssignment.setActive(true);
        adminAssignment.setAssignmentOrigin("DEV");

        SubjectRoleAssignmentEntity auditorAssignment = new SubjectRoleAssignmentEntity();
        auditorAssignment.setSubjectCode("bifor");
        auditorAssignment.setRoleCode("AUDITOR");
        auditorAssignment.setActive(true);
        auditorAssignment.setAssignmentOrigin("INTERNAL");

        SubjectRoleAssignmentEntity readonlyAssignment = new SubjectRoleAssignmentEntity();
        readonlyAssignment.setSubjectCode("bifor");
        readonlyAssignment.setRoleCode("READONLY");
        readonlyAssignment.setActive(true);
        readonlyAssignment.setAssignmentOrigin("SYNC");

        when(springDataRepository.findBySubjectCodeAndActiveTrue("bifor"))
            .thenReturn(List.of(adminAssignment, auditorAssignment, readonlyAssignment));

        assertThat(adapter.findActiveRoleCodesBySubjectCode("bifor"))
            .containsExactlyInAnyOrder("ADMIN", "AUDITOR", "READONLY");
    }

    @Test
    void failsFastOnInvalidAssignmentOrigin() {
        SubjectRoleAssignmentEntity invalidAssignment = new SubjectRoleAssignmentEntity();
        invalidAssignment.setSubjectCode("bifor");
        invalidAssignment.setRoleCode("ADMIN");
        invalidAssignment.setActive(true);
        invalidAssignment.setAssignmentOrigin("BROKEN");

        when(springDataRepository.findBySubjectCodeAndActiveTrue("bifor"))
                .thenReturn(List.of(invalidAssignment));

        assertThrows(IllegalStateException.class, () -> adapter.findActiveRoleCodesBySubjectCode("bifor"));
    }
}