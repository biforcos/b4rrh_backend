package com.b4rrhh.authorization.application.usecase;

import com.b4rrhh.authorization.domain.port.SubjectRoleAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResolveSubjectRolesService implements ResolveSubjectRolesUseCase {

    private final SubjectRoleAssignmentRepository subjectRoleAssignmentRepository;

    public ResolveSubjectRolesService(SubjectRoleAssignmentRepository subjectRoleAssignmentRepository) {
        this.subjectRoleAssignmentRepository = subjectRoleAssignmentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> resolveActiveRoleCodes(String subjectCode) {
        return subjectRoleAssignmentRepository.findActiveRoleCodesBySubjectCode(normalizeSubjectCode(subjectCode));
    }

    private String normalizeSubjectCode(String subjectCode) {
        if (subjectCode == null || subjectCode.isBlank()) {
            throw new IllegalArgumentException("subjectCode is required");
        }
        // subjectCode is an opaque authenticated identifier; trim only, never case-normalize.
        return subjectCode.trim();
    }
}