package com.b4rrhh.authorization.infrastructure.persistence;

import com.b4rrhh.authorization.domain.model.SubjectRoleAssignmentOrigin;
import com.b4rrhh.authorization.domain.port.SubjectRoleAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubjectRoleAssignmentPersistenceAdapter implements SubjectRoleAssignmentRepository {

    private final SpringDataSubjectRoleAssignmentRepository springDataRepository;

    public SubjectRoleAssignmentPersistenceAdapter(SpringDataSubjectRoleAssignmentRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public List<String> findActiveRoleCodesBySubjectCode(String subjectCode) {
        return springDataRepository.findBySubjectCodeAndActiveTrue(subjectCode).stream()
                .map(entity -> {
                    validateAssignmentOrigin(entity);
                    return entity.getRoleCode();
                })
                .toList();
    }

    private void validateAssignmentOrigin(SubjectRoleAssignmentEntity entity) {
        try {
            SubjectRoleAssignmentOrigin.valueOf(entity.getAssignmentOrigin());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid assignment_origin in database for subject-role assignment ("
                            + entity.getSubjectCode() + ", " + entity.getRoleCode() + "): "
                            + entity.getAssignmentOrigin(),
                    e
            );
        }
    }
}