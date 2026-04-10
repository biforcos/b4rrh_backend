package com.b4rrhh.authorization.application.usecase;

import com.b4rrhh.authorization.domain.port.SubjectRoleAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveSubjectRolesServiceTest {

    @Mock
    private SubjectRoleAssignmentRepository subjectRoleAssignmentRepository;

    private ResolveSubjectRolesService service;

    @BeforeEach
    void setUp() {
        service = new ResolveSubjectRolesService(subjectRoleAssignmentRepository);
    }

    @Test
    void trimsSubjectCodeBeforeResolvingRolesWithoutChangingCase() {
        when(subjectRoleAssignmentRepository.findActiveRoleCodesBySubjectCode("bifor"))
                .thenReturn(List.of("ADMIN"));

        List<String> roleCodes = service.resolveActiveRoleCodes(" bifor ");

        assertEquals(List.of("ADMIN"), roleCodes);
        verify(subjectRoleAssignmentRepository).findActiveRoleCodesBySubjectCode("bifor");
    }

    @Test
    void returnsEmptyListWhenSubjectHasNoAssignments() {
        when(subjectRoleAssignmentRepository.findActiveRoleCodesBySubjectCode("bifor"))
                .thenReturn(List.of());

        List<String> roleCodes = service.resolveActiveRoleCodes("bifor");

        assertEquals(List.of(), roleCodes);
        verify(subjectRoleAssignmentRepository).findActiveRoleCodesBySubjectCode("bifor");
    }

    @Test
    void preservesSubjectCaseWhenResolvingRoles() {
        when(subjectRoleAssignmentRepository.findActiveRoleCodesBySubjectCode("BiFor"))
                .thenReturn(List.of("ADMIN"));

        List<String> roleCodes = service.resolveActiveRoleCodes(" BiFor ");

        assertEquals(List.of("ADMIN"), roleCodes);
        verify(subjectRoleAssignmentRepository).findActiveRoleCodesBySubjectCode("BiFor");
    }

    @Test
    void rejectsBlankSubjectCode() {
        assertThrows(IllegalArgumentException.class, () -> service.resolveActiveRoleCodes(" "));
    }

    @Test
    void rejectsNullSubjectCode() {
        assertThrows(IllegalArgumentException.class, () -> service.resolveActiveRoleCodes(null));
    }
}